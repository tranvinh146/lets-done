package com.iconloop.score.example;

import score.*;

public class Pool {
    private boolean active;
    private String taskIds;
    private String starers;
    private int totalStars;

    public Pool() {
        this.active = true;
        this.taskIds = "";
        this.starers = "";
        this.totalStars = 0;
    }

    public static void writeObject(ObjectWriter w, Pool p) {
        w.beginList(3);
        w.write(
                p.active,
                p.taskIds,
                p.starers,
                p.totalStars
        );
        w.end();
    }

    public static Pool readObject(ObjectReader r) {
        r.beginList();
        Pool p = new Pool();
        p.setActive(r.readBoolean());
        p.setTaskIds(r.readString());
        p.setStarers(r.readString());
        p.setTotalStars(r.readInt());
        r.end();
        return p;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public void setStarers(String starers) {
        this.starers = starers;
    }

    public void setTotalStars(int totalStars) {
        this.totalStars = totalStars;
    }

    public void addStar(String address) {
        boolean exists = checkStar(address);
        Context.require(!exists, "Already gave star");
        starers = starers.concat(address + ",");
        totalStars += 1;
    }

    public void removeStar(String address) {
        boolean exists = checkStar(address);
        Context.require(exists, "Have not give star yet");
        starers = starers.replace(address + ",", "");
        totalStars -= 1;
    }

    public void addTaskId(String taskId) {
        boolean exists = taskIds.contains(taskId);
        Context.require(!exists, "Task Id already existed");
        taskIds = taskIds.concat(taskId + ",");
    }

    public void removeTaskId(String taskId) {
        boolean exists = taskIds.contains(taskId);
        Context.require(exists, "Not found task Id");
        taskIds = taskIds.replace(taskId + ",", "");
    }

    public boolean checkStar(String address) {
        return starers.contains(address);
    }

    public int getTotalStars() {
        return totalStars;
    }

    public boolean getActive() {
        return active;
    }

    public String getTaskIds() {
        return taskIds;
    }
}