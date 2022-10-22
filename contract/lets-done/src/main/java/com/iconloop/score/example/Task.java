package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.ObjectReader;
import score.ObjectWriter;
import score.annotation.External;

import java.math.BigInteger;

import static com.iconloop.score.example.LetsDone.ONE_ICX;

public class Task {
    private final long DURATION = 10 * 60 * 1_000_000; // 10 minutes to microseconds
    private final Address creator;
    private final BigInteger amount;
    private final String content;
    private final long createdTime;
    private String voters;
    private int votedAmount;

    public Task(Address creator, BigInteger amount, String content, long createdTime) {
        this.creator = creator;
        this.amount = amount;
        this.content = content;
        this.createdTime = createdTime;
        this.voters = "";
        this.votedAmount = 0;
    }

    public static void writeObject(ObjectWriter w, Task t) {
        w.beginList(3);
        w.writeNullable(
                t.creator,
                t.amount,
                t.content,
                t.createdTime
        );
        w.write(
                t.voters,
                t.votedAmount
        );
        w.end();
    }

    public static Task readObject(ObjectReader r) {
        r.beginList();
        Task t = new Task(
                r.readAddress(),
                r.readBigInteger(),
                r.readString(),
                r.readLong()
        );
        t.setVoters(r.readString());
        t.setVotedAmount(r.readInt());
        r.end();
        return t;
    }

    public void setVoters(String voters) {
        this.voters = voters;
    }

    public void setVotedAmount(int votedAmount) {
        this.votedAmount = votedAmount;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public boolean voteAndCheckCompleted(String voterAddress) {
        checkTimeVote();

        Context.require(!voters.contains(voterAddress), "Already voted");

        int requiredVote = getRequiredVote(amount);
        votedAmount += 1;
        voters = voters.concat(voterAddress);
        Context.require(votedAmount <= requiredVote, "Enough votes");
        return votedAmount == requiredVote;
    }

    public String toJsonFormat(String taskId) {
        long expiredTime = (createdTime + DURATION) / 1000;
        int requiredVote = getRequiredVote(amount);
        return "{" +
                "\"taskId\":\"" + taskId +
                "\",\"creator\":\"" + creator +
                "\",\"reward\":\"" + amount +
                "\",\"content\":\"" + content +
                "\",\"expiredAt\":" + expiredTime +
                ",\"requiredVote\":" + requiredVote +
                ",\"votedAmount\":" + votedAmount +
                "}";
    }

    public int getRequiredVote(BigInteger amount) {
        int amountICX = amount.divide(ONE_ICX).intValue();
        return amountICX * 2;
    }

    public Address getCreator() {
        return creator;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public String getContent() {
        return content;
    }

    public int getVotedAmount() {
        return votedAmount;
    }

    public void checkTimeVote() {
        long currentTime = Context.getBlockTimestamp();
        Context.require(currentTime <= createdTime + DURATION, "Time to vote is expired");
    }

    public void checkTaskUndone() {
        long currentTime = Context.getBlockTimestamp();
        Context.require(currentTime > createdTime + DURATION, "Task is being done");
    }
}

