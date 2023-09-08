import {menuGetItem} from "@/lib/Utils";
import {AiOutlineNotification} from "react-icons/ai";
import {HiOutlineChatBubbleLeftRight} from "react-icons/hi2";
import {BsChatDots} from "react-icons/bs";
import {Badge, Menu} from "antd";
import {useEffect, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {selectHasNewPublicNotification} from "@/lib/redux/slices/message/hasNewPublicNotificationSlice";
import {updateHasMessage} from "@/lib/redux/slices/message/hasMessageSlice";
import {selectHasNewPrivateNotification} from "@/lib/redux/slices/message/hasNewPrivateNotificationSlice";
import {selectHasNewContactMessage} from "@/lib/redux/slices/message/hasNewContactMessageSlice";
import {selectHasNewGroupMessage} from "@/lib/redux/slices/message/hasNewGroupMessageSlice";

export default function MessageMenu({changeMode, notificationApi}) {

    const dispatch = useDispatch()
    const hasNewPublicNotification = useSelector(selectHasNewPublicNotification)
    const hasNewPrivateNotification = useSelector(selectHasNewPrivateNotification)
    const hasNewContactMessage = useSelector(selectHasNewContactMessage)
    const hasNewGroupMessage = useSelector(selectHasNewGroupMessage)
    const [hasNotification, setHasNotification] = useState(false)
    const [hasGroupMessage, setHasGroupMessage] = useState(false)
    const [hasContactMessage, setHasContactMessage] = useState(false)

    const clearHasMessage = () => {
        if (!hasNotification && !hasGroupMessage && !hasContactMessage) {
            dispatch(updateHasMessage(false))
        }
    }

    useEffect(() => {
        setHasNotification(hasNewPublicNotification.hasNew)
        clearHasMessage()
    }, [hasNewPublicNotification])

    useEffect(() => {
        setHasNotification(hasNewPrivateNotification.hasNew)
        clearHasMessage()
    }, [hasNewPrivateNotification])

    useEffect(() => {
        setHasNotification(hasNewContactMessage.hasNew)
        clearHasMessage()
    }, [hasNewContactMessage])

    useEffect(() => {
        setHasGroupMessage(hasNewGroupMessage.hasNew)
        clearHasMessage()
    }, [hasNewGroupMessage])

    return (
        <Menu
            defaultSelectedKeys={['notification']}
            items={[
                menuGetItem(
                    "Notification",
                    "notification",
                    <Badge dot={hasNotification}>
                        <AiOutlineNotification/>
                    </Badge>
                ),
                menuGetItem(
                    "Group Chat",
                    "groupChat",
                    <Badge dot={hasGroupMessage}>
                        <HiOutlineChatBubbleLeftRight/>
                    </Badge>
                ),
                menuGetItem(
                    "Contact Message",
                    "contactMessage",
                    <Badge dot={hasContactMessage}>
                        <BsChatDots/>
                    </Badge>
                )
            ]}
            onClick={(item) => {
                changeMode(item.key)
            }}
        />
    )
}