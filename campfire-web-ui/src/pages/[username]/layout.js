import {Avatar, Badge, Button, Col, Divider, Dropdown, Layout, notification, Row, Space, Tooltip} from "antd";
import {useDispatch, useSelector} from "react-redux";
import {selectUsername, updateUsername} from "@/lib/redux/slices/usernameSlice";
import {selectAvatarUrl, updateAvatarUrl} from "@/lib/redux/slices/avatarUrlSlice";
import {useRouter} from "next/router";
import {useEffect, useState} from "react";
import axios from "axios";
import {updateAccountId} from "@/lib/redux/slices/accountIdSlice";
import {updateAccountStatus} from "@/lib/redux/slices/accountStatusSlice";
import {updateRoleNames} from "@/lib/redux/slices/roleNamesSlice";
import {selectHasMessage, updateHasMessage} from "@/lib/redux/slices/message/hasMessageSlice";
import {HiOutlineStatusOffline, HiOutlineStatusOnline} from "react-icons/hi";
import {selectWsConnected, updateWsConnected} from "@/lib/redux/slices/wsConnectedSlice";
import {wsClient} from "@/lib/Utils";
import {updateHasNewPublicNotification} from "@/lib/redux/slices/message/hasNewPublicNotificationSlice";
import {updateHasNewPrivateNotification} from "@/lib/redux/slices/message/hasNewPrivateNotificationSlice";
import {updateHasNewContactMessage} from "@/lib/redux/slices/message/hasNewContactMessageSlice";
import {updateHasNewGroupMessage} from "@/lib/redux/slices/message/hasNewGroupMessageSlice";

const {Header, Content, Footer} = Layout

export const ssr = false;

const HeaderBtnList = () => {

    const dispatch = useDispatch()
    const username = useSelector(selectUsername)
    const hasMessage = useSelector(selectHasMessage)
    const [lastSelected, setLastSelected] = useState("")

    function HeaderBtnTextRender({text}) {
        const fontSize = "16px"
        const pageBtnInitialStyle = {
            color: "black",
            fontSize: fontSize
        }
        const pageBtnHoverStyle = {
            color: "#1677ff",
            fontSize: fontSize
        }
        const [pageBtnStyle, setPageBtnStyle] = useState(pageBtnInitialStyle)

        return (
            <span
                style={pageBtnStyle}
                onMouseEnter={() => setPageBtnStyle(pageBtnHoverStyle)}
                onMouseLeave={() => setPageBtnStyle(pageBtnInitialStyle)}
            >
                {text}
            </span>
        )
    }

    const btnClick = (id) => {
        if (lastSelected === "message") {
            dispatch(updateHasMessage(false))
        }
        setLastSelected(id)
    }

    return (
        <>
            <Space>
                <Button type="link" href={"/" + username} onClick={() => btnClick("profile")}>
                    <HeaderBtnTextRender text="Profile"/>
                </Button>
                <Divider type="vertical"/>
                <Button type="link" href={"/" + username + "/group"} onClick={() => btnClick("group")}>
                    <HeaderBtnTextRender text="Group"/>
                </Button>
                <Divider type="vertical"/>
                <Button type="link" href={"/" + username + "/task"} onClick={() => btnClick("task")}>
                    <HeaderBtnTextRender text="Task"/>
                </Button>
                <Divider type="vertical"/>
                <Button type="link" href={"/" + username + "/note"} onClick={() => btnClick("note")}>
                    <HeaderBtnTextRender text="Note"/>
                </Button>
                <Divider type="vertical"/>
                <Badge dot={hasMessage === true}>
                    <Button type="link" href={"/" + username + "/message"} onClick={() => btnClick("message")}>
                        <HeaderBtnTextRender text="Message"/>
                    </Button>
                </Badge>
            </Space>
        </>
    )
}

export default function MainLayout({children}) {

    const router = useRouter()
    const dispatch = useDispatch()
    const username = useSelector(selectUsername)
    const avatarUrl = useSelector(selectAvatarUrl)
    const wsConnected = useSelector(selectWsConnected)
    const [notificationApi, contextHolder] = notification.useNotification();
    const dropdownItems = [
        {
            key: "contact",
            label: (<Button type="link" href={"/" + username + "/contact"}>My Contact</Button>),
        },
        {
            key: "setting",
            label: (<Button type="link" href={"/" + username + "/setting"}>Setting</Button>),
        },
        {
            key: "signOut",
            label: (<Button type="link" href="/" onClick={() => wsClient.deactivate()}>Sign Out</Button>),
        },
    ];

    const initWebSocket = (accountId, csrfToken) => {
        wsClient.brokerURL = "ws://localhost/ws-connect?accountId=" + accountId
        wsClient.connectHeaders = {
            "X-CSRF-TOKEN": csrfToken
        }
        wsClient.onConnect = () => {
            if (wsClient.connected) {
                dispatch(updateWsConnected(true))
                wsClient.subscribe(
                    "/topic/public-notification",
                    resp => {
                        const body = JSON.parse(resp.body)
                        dispatch(updateHasMessage(true))
                        dispatch(updateHasNewPublicNotification({
                            hasNew: true,
                            id: body.id
                        }))
                    }
                )
                wsClient.subscribe(
                    "/user/queue/private-notification",
                    resp => {
                        const body = JSON.parse(resp.body)
                        dispatch(updateHasMessage(true))
                        dispatch(updateHasNewPrivateNotification({
                            hasNew: true,
                            id: body.id
                        }))
                    }
                )
                wsClient.subscribe(
                    "/user/queue/contact-chat",
                    resp => {
                        const body = JSON.parse(resp.body)
                        dispatch(updateHasMessage(true))
                        dispatch(updateHasNewContactMessage({
                            hasNew: true,
                            ...body
                        }))
                    }
                )
                wsClient.subscribe(
                    "/user/queue/group-chat",
                    resp => {
                        const body = JSON.parse(resp.body)
                        // console.log(body)
                        dispatch(updateHasMessage(true))
                        dispatch(updateHasNewGroupMessage({
                            hasNew: true,
                            ...body
                        }))
                    }
                )
            }
        }
        wsClient.onDisconnect = () => {
            console.log("websocket disconnect")
            dispatch(updateWsConnected(false))
        }
        wsClient.onWebSocketClose = (evt) => {
            dispatch(updateWsConnected(false))
        }
        wsClient.onStompError = (frame) => {
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
            dispatch(updateWsConnected(false))
        }
        wsClient.activate()
    }

    useEffect(() => {
        if (router.query && router.query.username) {
            const {username: urlUsername} = router.query
            axios.get(
                "/api/account/authenticate"
            ).then(resp => {
                let result = false
                let resultDescription = "Try to Sign In again Later"
                if (resp.data !== null) {
                    const {code, message, data} = resp.data
                    if (code === 200) {
                        if (data.username !== urlUsername) {
                            router.replace("/")
                        }
                        if (data.roleNames.includes("regular_user")) {
                            result = true;
                            initWebSocket(data.id, data.csrfToken)
                            axios.get(
                                "/api/account/id/" + data.id
                            ).then(resp => {
                                if (resp.data !== null) {
                                    const {code, message, data} = resp.data
                                    if (code === 200) {
                                        dispatch(updateAvatarUrl(data.avatarUrl))
                                    }
                                }
                            }).catch(err => {
                            });
                        }
                    } else {
                        resultDescription = message
                    }
                }
                if (!result) {
                    notificationApi.error({
                        message: "Authentication Failure",
                        description: resultDescription
                    })
                    setTimeout(() => {
                        router.replace("/sign-in")
                    }, 2000)
                }
            }).catch(err => {
                console.log(err)
                dispatch(updateAccountId(""))
                dispatch(updateUsername(""))
                dispatch(updateAccountStatus(""))
                dispatch(updateRoleNames(""))
                dispatch(updateAvatarUrl(""))
                router.replace("/")
            });
        }
    }, [router.isReady]);

    return (
        <>
            {contextHolder}
            <Layout style={{height: "100vh"}}>
                <Header style={{backgroundColor: "white"}}>
                    <Row>
                        <Col span={12}>
                            <HeaderBtnList/>
                        </Col>
                        <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                            <Space direction="horizontal">
                                {
                                    wsConnected ?
                                        <div style={{display: "flex", alignItems: "center", color: "green"}}>
                                            <HiOutlineStatusOnline/>
                                            <span>&nbsp;Online</span>
                                        </div> :
                                        <div style={{display: "flex", alignItems: "center", color: "red"}}>
                                            <Tooltip placement="bottom"
                                                     title="Try to refresh or sign out and sign in again">
                                                <HiOutlineStatusOffline/>
                                                <span>&nbsp;Offline</span>
                                            </Tooltip>
                                        </div>
                                }
                                <Divider type="vertical"/>
                                <Dropdown menu={{items: dropdownItems}} arrow>
                                    <Button type="text">
                                        <span style={{fontWeight: "bold"}}>{username}</span>
                                    </Button>
                                </Dropdown>
                                <Divider type="vertical"/>
                                <Avatar shape="square" size="large" src={avatarUrl}/>
                            </Space>
                        </Col>
                    </Row>
                </Header>
                <Divider style={{margin: "0 0 0 0"}}/>
                <Content style={{backgroundColor: "white", overflow: "auto"}}>
                    <Content style={{height: "100%", overflow: "auto"}}>
                        {children}
                    </Content>
                    <Footer>
                        Copyright &copy; 2023 Campfire. All Rights Reserved
                    </Footer>
                </Content>
            </Layout>
        </>
    )
}