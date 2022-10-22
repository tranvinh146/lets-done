// /*
//  * Copyright 2020 ICONLOOP Inc.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *     http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

package com.iconloop.score.example;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Address;
import score.Context;

class LetsDoneTest extends TestBase {
    private static final BigInteger ONE_ICX = new BigInteger("1000000000000000000");

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount(10);
    private static final Account alice = sm.createAccount(10);
    private static final Account bob = sm.createAccount(10);
    private Score letsDoneScore;

    @BeforeEach
    public void setup() throws Exception {
        letsDoneScore = sm.deploy(owner, LetsDone.class);
    }

    @Test
    public void getNonExistPool() {
        Context.require(letsDoneScore.call("getPoolByName", "hello") == null);
    }

    @Test
    public void createPool() {
        letsDoneScore.invoke(owner, "createPool", "hello");
        Address poolOwner = (Address) letsDoneScore.call("getPoolByName", "hello");
        Context.require(poolOwner == owner.getAddress());
    }

    @Test
    public void checkStatusPool() {
        letsDoneScore.invoke(owner, "createPool", "hello");

        letsDoneScore.invoke(owner, "closePool");
        boolean status = (boolean) letsDoneScore.call("getPoolStatus", owner.getAddress());
        Context.require(!status);

        letsDoneScore.invoke(owner, "openPool");
        status = (boolean) letsDoneScore.call("getPoolStatus", owner.getAddress());
        Context.require(status);
    }

    @Test
    public void checkStarsInPool() {
        letsDoneScore.invoke(owner, "createPool", "hello");
        boolean isStar;
        int totalStars;

        totalStars = (int) letsDoneScore.call("getPoolStars", owner.getAddress());
        isStar = (boolean) letsDoneScore.call("checkStarPool", owner.getAddress(), alice.getAddress());
        Context.require(!isStar);
        Context.require(totalStars == 0);

        letsDoneScore.invoke(alice, "giveStar", owner.getAddress());

        totalStars = (int) letsDoneScore.call("getPoolStars", owner.getAddress());
        isStar = (boolean) letsDoneScore.call("checkStarPool", owner.getAddress(), alice.getAddress());
        Context.require(isStar);
        Context.require(totalStars == 1);

        letsDoneScore.invoke(alice, "retrieveStar", owner.getAddress());

        totalStars = (int) letsDoneScore.call("getPoolStars", owner.getAddress());
        isStar = (boolean) letsDoneScore.call("checkStarPool", owner.getAddress(), alice.getAddress());
        Context.require(!isStar);
        Context.require(totalStars == 0);
    }

    @Test
    public void getNotCreatedTask() {
        letsDoneScore.invoke(owner, "createPool", "hello");
        String taskId = "0";

        String taskInfo = (String) letsDoneScore.call("getTaskJson", owner.getAddress(), taskId);
        String taskIds = (String) letsDoneScore.call("getTaskIds", owner.getAddress());

        assertNull(taskInfo);
        assertNull(taskIds);
    }

    @Test
    public void createTask() {
        letsDoneScore.invoke(owner, "createPool", "hello");

        String taskId = "0";
        String taskContent = "sing a song";
        sm.call(alice, ONE_ICX, letsDoneScore.getAddress(), "createTask", owner.getAddress(), taskId, taskContent);
        String taskInfo = (String) letsDoneScore.call("getTaskJson", owner.getAddress(), taskId);
        String taskIds = (String) letsDoneScore.call("getTaskIds", owner.getAddress());

        Context.require(taskInfo.contains(taskId) && taskInfo.contains(taskContent));
        Context.require(taskIds.contains(taskId));
    }

    @Test
    public void rejectTask() {
        letsDoneScore.invoke(owner, "createPool", "hello");

        String taskId = "1";
        String taskContent = "sing a song";

        sm.call(alice, ONE_ICX, letsDoneScore.getAddress(), "createTask", owner.getAddress(), taskId, taskContent);
        sm.transfer(alice, letsDoneScore.getAddress(), ONE_ICX);
        letsDoneScore.invoke(owner, "rejectTask", taskId);

        String taskInfo = (String) letsDoneScore.call("getTaskJson", owner.getAddress(), taskId);
        String taskIds = (String) letsDoneScore.call("getTaskIds", owner.getAddress());

        assertNull(taskInfo);
        assertNull(taskIds);
    }

    @Test
    public void voteTask() {
        letsDoneScore.invoke(owner, "createPool", "hello");
        String taskId = "1";
        String taskContent = "sing a song";
        sm.call(alice, ONE_ICX, letsDoneScore.getAddress(), "createTask", owner.getAddress(), taskId, taskContent);
        sm.transfer(alice, letsDoneScore.getAddress(), ONE_ICX);

        letsDoneScore.invoke(alice, "voteTask", owner.getAddress(), taskId);
        sm.getBlock().increase(10 * 30);
        assertThrows(AssertionError.class, () -> {
            letsDoneScore.invoke(bob, "voteTask", owner.getAddress(), taskId);
        });
    }

    @Test
    public void withdrawTask() {
        letsDoneScore.invoke(owner, "createPool", "hello");
        String taskId = "1";
        String taskContent = "sing a song";
        sm.call(alice, ONE_ICX, letsDoneScore.getAddress(), "createTask", owner.getAddress(), taskId, taskContent);
        sm.transfer(alice, letsDoneScore.getAddress(), ONE_ICX);
        sm.getBlock().increase(10 * 30);
        letsDoneScore.invoke(alice, "withdrawTask", owner.getAddress(), taskId);
    }
}



