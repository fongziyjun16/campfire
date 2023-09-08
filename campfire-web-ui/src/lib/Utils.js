import {Client} from "@stomp/stompjs";

export const menuGetItem = (label, key, icon, children, type) => ({
    key,
    icon,
    children,
    label,
    type,
})

export const formatTime = (time) => {
    return time?.substring(0, time.indexOf('.'))
}

export const wsClient = new Client({
    // debug: (str) => {
    //     console.log(str)
    // }
})