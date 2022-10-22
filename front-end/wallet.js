const IconService = window["icon-sdk-js"].default;
const httpProvider = new IconService.HttpProvider(
    "https://sejong.net.solidwallet.io/api/v3/"
);
const iconService = new IconService(httpProvider);
const iconConverter = IconService.IconConverter;
const iconAmount = IconService.IconAmount;
const iconWallet = IconService.IconWallet;
const iconBuilder = IconService.IconBuilder;
const SignedTransaction = IconService.SignedTransaction;

const connectBtn = document.getElementById("connect-btn");
const accountInfo = document.getElementById("account-info");

const contractAddress = "cxec5d0e18637eaea4102fea801d27394f2a4e33da";

let myAddress, myBalance;

if (localStorage.getItem("walletAddress")) {
    successfulConnect();
} else {
    successfulDisconnect();
}

async function successfulConnect() {
    connectBtn.innerHTML = "Disconnect Wallet";
    connectBtn.classList.add("btn-danger");
    connectBtn.classList.remove("btn-primary");
    connectBtn.onclick = disconnectWallet;

    myAddress = localStorage.getItem("walletAddress");
    myBalance = await iconService
        .getBalance(myAddress)
        .execute();
    myBalance = iconConverter.toNumber(myBalance);
    myBalance = iconAmount.of(myBalance, 0).convertUnit(18);

    accountInfo.innerHTML = `
        <div>Wallet Address: ${myAddress} </div>
        <div>Balance: ${myBalance} ICX</div>
    `;
}

function connectWallet() {
    window.dispatchEvent(new CustomEvent('ICONEX_RELAY_REQUEST', {
        detail: {
            type: 'REQUEST_ADDRESS'
        }
    }));

    const eventHandler = async (event) => {
        const { type, payload } = event.detail;

        if (type == 'RESPONSE_ADDRESS') {
            localStorage.setItem("walletAddress", payload);
            await successfulConnect();
        }
    };

    window.addEventListener("ICONEX_RELAY_RESPONSE", eventHandler);

}

function successfulDisconnect() {
    connectBtn.innerHTML = "Connect Wallet";
    connectBtn.classList.add("btn-primary");
    connectBtn.classList.remove("btn-danger");
    connectBtn.onclick = connectWallet;

    myAddress = null;
    accountInfo.innerText = "Please connect to wallet!"
}

function disconnectWallet() {
    localStorage.removeItem("walletAddress");

    successfulDisconnect();
}

