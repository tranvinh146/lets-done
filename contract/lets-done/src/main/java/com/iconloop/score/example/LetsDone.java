/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;

import score.Address;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;

public class LetsDone {
    public static final BigInteger ONE_ICX = new BigInteger("1000000000000000000");
//    private final DictDB<Address, BigInteger> balances = Context.newDictDB("balances", BigInteger.class);
    private final DictDB<Address, Pool> pools  = Context.newDictDB("pools", Pool.class);
    private final DictDB<String, Address> poolNames = Context.newDictDB("poolNames", Address.class);
    private final BranchDB<Address, DictDB<String, Task>> poolTasks = Context.newBranchDB("poolTasks", Task.class);

    public LetsDone() {

    }

    @Payable
    public void fallback() {}

    @External
    public void createPool(String poolName) {
        Context.require(poolNames.get(poolName) == null, "Name already exists");

        Address owner = Context.getCaller();
        Context.require(pools.get(owner) == null, "You already created pool.");

        Pool newPool = new Pool();
        pools.set(owner, newPool);
        poolNames.set(poolName, owner);
    }

    @External
    public void closePool() {
        Address owner = Context.getCaller();
        Pool pool = checkAndGetPool(owner);
        pool.setActive(false);
        pools.set(owner, pool);
    }

    @External
    public void openPool() {
        Address owner = Context.getCaller();
        Pool pool = checkAndGetPool(owner);
        pool.setActive(true);
        pools.set(owner, pool);
    }

    @External
    public void giveStar(Address owner) {
        Address caller = Context.getCaller();
        Pool pool = checkAndGetPool(owner);
        pool.addStar(caller.toString());
        pools.set(owner, pool);
    }

    @External
    public void retrieveStar(Address owner) {
        Address caller = Context.getCaller();
        Pool pool = checkAndGetPool(owner);
        pool.removeStar(caller.toString());
        pools.set(owner, pool);
    }

    @External
    @Payable
    public void createTask(Address poolOwner, String taskId, String content) {
        Pool pool = checkAndGetPool(poolOwner);

        Address creator = Context.getCaller();
        BigInteger amount = Context.getValue();
        Context.require(amount.compareTo(ONE_ICX) >= 0, "At least 1 ICX");

        pool.addTaskId(taskId);
        pools.set(poolOwner, pool);
        poolTasks.at(poolOwner).set(taskId, new Task(creator, amount, content, Context.getBlockTimestamp()));
    }

    @External
    public void rejectTask(String taskId) {
        Address poolOwner = Context.getCaller();
        Pool pool = checkAndGetPool(poolOwner);
        Task task = checkAndGetTask(poolOwner, taskId);

        pool.removeTaskId(taskId);
        pools.set(poolOwner, pool);

        assert task != null;
        Address creator = task.getCreator();
        BigInteger amount = task.getAmount();

//        BigInteger creatorBalance = getSafeBalance(creator);
        poolTasks.at(poolOwner).set(taskId, null);
        Context.transfer(creator, amount);
//        balances.set(creator, creatorBalance.add(amount));
    }

    @External
    public void voteTask(Address poolOwner, String taskId) {
        Address voter = Context.getCaller();

        Pool pool = checkAndGetPool(poolOwner);
        Task task = checkAndGetTask(poolOwner, taskId);

        boolean isCompleted = task.voteAndCheckCompleted(voter.toString());

        if (isCompleted) {
            pool.removeTaskId(taskId);
            pools.set(poolOwner, pool);
            poolTasks.at(poolOwner).set(taskId, null);

            BigInteger reward = task.getAmount();
            Context.transfer(poolOwner, reward);
        } else {
            poolTasks.at(poolOwner).set(taskId, task);
        }
    }

    @External
    public void withdrawTask(Address poolOwner, String taskId) {
        Address caller = Context.getCaller();

        Pool pool = checkAndGetPool(poolOwner);
        Task task = checkAndGetTask(poolOwner, taskId);

        Context.require(task.getCreator().equals(caller), "Only creator can call");
        task.checkTaskUndone();

        pool.removeTaskId(taskId);
        pools.set(poolOwner, pool);
        poolTasks.at(poolOwner).set(taskId, null);

        BigInteger amount = task.getAmount();
        Context.transfer(caller, amount);
    }

    @External(readonly = true)
    public Address getPoolByName(String name) {
        return poolNames.getOrDefault(name, null);
    }

//    @External(readonly = true)
//    public BigInteger getSafeBalance(Address owner) {
//        return balances.getOrDefault(owner, BigInteger.ZERO);
//    }

    @External(readonly = true)
    public int getPoolStars(Address owner) {
        Pool pool = checkAndGetPool(owner);
        return pool.getTotalStars();
    }

    @External(readonly = true)
    public boolean checkStarPool(Address owner, Address caller) {
        Pool pool = checkAndGetPool(owner);
        return pool.checkStar(caller.toString());
    }

    @External(readonly = true)
    public boolean getPoolStatus(Address owner) {
        Pool pool = checkAndGetPool(owner);
        return pool.getActive();
    }

    @External(readonly = true)
    public String getTaskJson(Address poolOwner, String taskId) {
        Task task = poolTasks.at(poolOwner).get(taskId);
        if (task == null) {
            return null;
        } else {
            return task.toJsonFormat(taskId);
        }
    }

    @External(readonly = true)
    public String getTaskIds(Address poolOwner) {
        Pool pool = checkAndGetPool(poolOwner);
        if (pool.getTaskIds().length() == 0) {
            return null;
        } else {
            return pool.getTaskIds();
        }
    }

    private Pool checkAndGetPool(Address owner) {
        Pool pool = pools.get(owner);
        Context.require(pool != null, "Pool have not been created yet");
        return pool;
    }

    private Task checkAndGetTask(Address poolOwner, String taskId) {
        Task task = poolTasks.at(poolOwner).get(taskId);
        Context.require(task != null, "Task with Id have not been created yet");
        return task;
    }
}
