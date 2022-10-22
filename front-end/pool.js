// import { callMethod, getMethod } from "./utils.js";

const createBtn = document.getElementById("create-btn")
const joinBtn = document.getElementById("join-btn");
const createPoolName = document.getElementById('create-pool');
const joinPoolName = document.getElementById('join-pool');

const poolNameEl = document.getElementById("pool-name");
const poolOwnerEl = document.getElementById("pool-owner");
const starsEl = document.getElementById("pool-stars");
const starBtn = document.getElementById("star-btn");

let poolName, poolOwner, isStar;

main();

function main() {
    if (window.location.pathname === "/index.html") {
        joinBtn.onclick = joinPool;
        createBtn.onclick = createPool;
    }
    if (window.location.pathname === "/pool.html") {
        poolName = localStorage.getItem("poolName");
        poolOwner = localStorage.getItem("poolOwner");

        poolNameEl.innerHTML = poolName;
        poolOwnerEl.innerHTML = poolOwner;

        window.addEventListener('load', loadPoolInfo);
    }
}
async function loadPoolInfo() {
    await getStars();
    await checkStar();
    window.removeEventListener('load', loadPoolInfo)
}

function createPool() {
    poolName = createPoolName.value;
    if (poolName) {
        callMethod({
            from: myAddress,
            to: contractAddress,
            method: "createPool",
            params: {
                poolName
            }
        }, (payload) => {
            localStorage.setItem("poolOwner", myAddress);
            localStorage.setItem("poolName", poolName);
            window.location.href = 'pool.html';
        });
    } else {
        console.log('empty')
    }
}

function joinPool() {
    poolName = joinPoolName.value;
    if (poolName) {
        getMethod({
            from: myAddress,
            to: contractAddress,
            method: "getPoolByName",
            params: {
                name: poolName
            }
        }, (res) => {
            const poolOwner = res;
            if (poolOwner) {
                localStorage.setItem("poolOwner", poolOwner);
                localStorage.setItem("poolName", poolName);
                window.location.href = 'pool.html';
            } else {
                console.log('can not find')
            }
        });
    } else {
        console.log('empty')
    }
}

async function getStars() {
    if (poolName) {
        await getMethod({
            from: myAddress,
            to: contractAddress,
            method: "getPoolStars",
            params: {
                owner: poolOwner
            }
        }, (res) => {
            const stars = iconConverter.toNumber(res);
            starsEl.innerText = `${stars} stars`;
        });
    }
}

async function checkStar() {
    if (poolName) {
        await getMethod({
            from: myAddress,
            to: contractAddress,
            method: "checkStarPool",
            params: {
                owner: poolOwner,
                caller: myAddress
            }
        }, (res) => {
            isStar = !!iconConverter.toNumber(res);
            showStarInfo();
        });
    }
}

function showStarInfo() {
    starsEl.classList.remove(isStar ? "btn-primary" : "btn-success");
    starsEl.classList.add(isStar ? "btn-success" : "btn-primary");

    starBtn.classList.remove(isStar ? "btn-outline-pimary" : "btn-outline-success");
    starBtn.classList.add(isStar ? "btn-outline-success" : "btn-outline-primary");
    starBtn.innerHTML = isStar ? "unstar" : "star";

    starBtn.onclick = handleStar;
}

function handleStar() {
    callMethod({
        from: myAddress,
        to: contractAddress,
        method: isStar ? "retrieveStar" : "giveStar",
        params: {
            owner: poolOwner
        }
    }, async (payload) => {
        isStar = !isStar;
        setTimeout(getStars, 5000);
        showStarInfo();
    });
}