const callMethod = ({ from, to, method, params, value }, handleSuccess) => {
    const customEvent = new CustomEvent('ICONEX_RELAY_REQUEST', {
        detail: {
            type: 'REQUEST_JSON-RPC',
            payload: {
                jsonrpc: "2.0",
                method: "icx_sendTransaction",
                id: 133,
                params: iconConverter.toRawTransaction({
                    from,
                    to,
                    value: iconAmount.of(value || 0, iconAmount.Unit.ICX).toLoop(),
                    dataType: "call",
                    nid: "0x53",
                    timestamp: (new Date()).getTime() * 1000,
                    stepLimit: iconConverter.toBigNumber(1000000),
                    version: iconConverter.toBigNumber(3),
                    data: {
                        method,
                        params,
                    }
                })
            }
        }
    });
    window.dispatchEvent(customEvent);
    console.log(customEvent)

    const eventHandler = event => {
        window.removeEventListener('ICONEX_RELAY_RESPONSE', eventHandler);

        const { type, payload } = event.detail;
        if (type === 'RESPONSE_JSON-RPC') {
            handleSuccess(payload);
        }
        else if (type === 'CANCEL_JSON-RPC') {
            console.error('User cancelled JSON-RPC request');
        }
    }
    window.addEventListener('ICONEX_RELAY_RESPONSE', eventHandler);
}

async function getMethod({ from, to, method, params }, handleSuccess) {
    const customEvent = new CustomEvent('ICONEX_RELAY_REQUEST', {
        detail: {
            type: 'REQUEST_JSON-RPC',
            payload: {
                jsonrpc: "2.0",
                method: "icx_call",
                id: 133,
                params: {
                    from,
                    to,
                    dataType: "call",
                    data: {
                        method,
                        params,
                    }
                }
            }
        }
    });
    window.dispatchEvent(customEvent);

    await new Promise((resolve, reject) => {
        const eventHandler = event => {
            window.removeEventListener('ICONEX_RELAY_RESPONSE', eventHandler);

            const { type, payload } = event.detail;
            if (type === 'RESPONSE_JSON-RPC') {
                handleSuccess(payload.result);
                resolve();
            }
            else if (type === 'CANCEL_JSON-RPC') {
                console.error('User cancelled JSON-RPC request');
                reject();
            }
        }

        window.addEventListener('ICONEX_RELAY_RESPONSE', eventHandler);
    });
}

const taskInfoTemple = ({ taskId, content, creator, reward, expiredAt, requiredVote, votedAmount }) => {
    const creatorShort = creator.substring(0, 4) + "..." + creator.slice(-4);
    const rewardIcx = iconAmount.of(reward, 0).convertUnit(18);
    const expiredTime = new Date(expiredAt);
    const expiredFormat = `${expiredTime.getHours()}:${expiredTime.getMinutes()} ${expiredTime.getDate()}/${expiredTime.getMonth() + 1}/${expiredTime.getFullYear()}`;

    const poolOwner = localStorage.getItem("poolOwner");
    const rejectVoteBtn = myAddress == poolOwner ? `<div type="button" class="btn btn-danger me-2" onclick="handleReject('${taskId}')">Reject</div>` : "";
    const withdrawBtn = myAddress == creator ? `<div type="button" class="btn btn-secondary" onclick="handleWithdraw('${taskId}')">Withdraw</div>` : "";

    return `
        <div class="bg-light bg-opacity-25 card my-3" style="max-width: 540px">
            <div class="px-3 py-1 fst-italic text-muted">#${taskId}</div>
            <div class="text-bg-dark bg-opacity-50 px-3 py-2 fw-bold">${content}</div>
            <div class="px-3 py-2">
                <div>
                    <span class="fw-semibold">Creator:</span> 
                    <span class="fst-italic">${creatorShort}</span></div>
                <div>
                    <span class="fw-semibold">Amount:</span> 
                    ${rewardIcx} ICX
                </div>
                <div>
                    <span class="fw-semibold">Expired at:</span> 
                    ${expiredFormat}
                </div>
                <div>
                    <span class="fw-semibold">Vote:</span> 
                    ${votedAmount}/${requiredVote}
                </div>
                <div class="my-2">
                    <div type="button" class="btn btn-primary me-2" onclick="handleVote('${taskId}')">Vote</div>
                    ${rejectVoteBtn}
                    ${withdrawBtn}
                </div>
            </div>
        </div>
    `;
}

// export { callMethod, getMethod, taskInfoTemple };