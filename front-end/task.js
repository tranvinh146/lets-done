// import { callMethod, getMethod, taskInfoTemple } from "./utils.js";

const idTask = document.getElementById("id-task");
const descriptionTask = document.getElementById("description-task");
const rewardTask = document.getElementById("reward-task");
const submitBtn = document.getElementById("submit-btn");
const infoTasks = document.getElementById("info-tasks");

// const poolOwner = localStorage.getItem("poolOwner");

let taskIds;

async function main() {
    if (!poolOwner) {
        submitBtn.classList.add("disabled");
    }
    await getTaskIds();
    console.log(taskIds)
    if (taskIds) {
        for (let taskId of taskIds) {
            await getTaskInfo(taskId);
        }
    }
}

setTimeout(main, 1000);


submitBtn.onclick = () => {
    const taskId = idTask.value;
    const content = descriptionTask.value;
    const value = rewardTask.value;

    if (taskId && content && value) {
        callMethod({
            from: myAddress,
            to: contractAddress,
            method: "createTask",
            value,
            params: {
                poolOwner,
                taskId,
                content
            }
        }, (payload) => {

        });
    } else {
        console.log("empty");
    }
}

async function getTaskIds() {
    await getMethod({
        from: myAddress,
        to: contractAddress,
        method: "getTaskIds",
        params: {
            poolOwner
        }
    }, (res) => {
        if (res) {
            taskIds = res.split(",");
            taskIds.pop();
        }
    });
}

async function getTaskInfo(taskId) {
    await getMethod({
        from: myAddress,
        to: contractAddress,
        method: "getTaskJson",
        params: {
            poolOwner,
            taskId
        }
    }, (res) => {
        const task = JSON.parse(res);
        console.log(task)
        infoTasks.innerHTML += taskInfoTemple(task);
    });
}

function handleReject(taskId) {
    callMethod({
        from: myAddress,
        to: contractAddress,
        method: "rejectTask",
        params: {
            taskId: taskId,
        }
    }, (payload) => {

    });
}

function handleVote(taskId) {
    callMethod({
        from: myAddress,
        to: contractAddress,
        method: "voteTask",
        params: {
            poolOwner,
            taskId: taskId,
        }
    }, (payload) => {

    });
}

function handleWithdraw(taskId) {
    callMethod({
        from: myAddress,
        to: contractAddress,
        method: "withdrawTask",
        params: {
            poolOwner,
            taskId: taskId,
        }
    }, (payload) => {

    });
}

