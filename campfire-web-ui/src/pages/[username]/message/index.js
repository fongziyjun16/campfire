import MainLayout from "@/pages/[username]/layout";
import {Layout, notification} from "antd";
import MessageMenu from "@/pages/[username]/message/component/menu";
import {useEffect, useState} from "react";
import NotificationContent from "@/pages/[username]/message/component/content/NotificationContent";
import GroupChatContent from "@/pages/[username]/message/component/content/GroupChatContent";
import ContactMessageContent from "@/pages/[username]/message/component/content/ContactMessageContent";
import {useDispatch} from "react-redux";
import {updateHasMessage} from "@/lib/redux/slices/message/hasMessageSlice";

const {Sider, Content} = Layout;

function MessagePage() {

    const dispatch = useDispatch()
    const [notificationApi, contextHolder] = notification.useNotification();
    const [mode, setMode] = useState("notification")

    useEffect(() => {
        dispatch(updateHasMessage(false))
    }, []);

    const changeMode = (currentMode) => {
        setMode(currentMode)
    }

    const getContent = () => {
        switch (mode) {
            case "notification":
                return <NotificationContent notificationApi={notificationApi}/>
            case "groupChat":
                return <GroupChatContent notificationApi={notificationApi}/>
            case "contactMessage":
                return <ContactMessageContent notificationApi={notificationApi}/>
        }
    }

    return (
        <>
            {contextHolder}
            <Layout style={{height: "100%"}}>
                <Sider style={{backgroundColor: "white"}}>
                    <MessageMenu changeMode={changeMode} notificationApi={notificationApi}/>
                </Sider>
                <Content style={{backgroundColor: "white"}}>
                    {getContent()}
                </Content>
            </Layout>
        </>
    )
}

MessagePage.pageLayout = MainLayout

export default MessagePage