import {Button, Card, Divider, Form, Input, Layout, List, Space} from "antd";
import InfiniteScroll from 'react-infinite-scroll-component';
import {useDispatch, useSelector} from "react-redux";
import {useEffect, useRef, useState} from "react";
import ListItemsRender from "@/pages/[username]/message/component/content/ListItemsRender";
import axios from "axios";
import {selectUsername} from "@/lib/redux/slices/usernameSlice";
import {clearHasNewGroupMessage, selectHasNewGroupMessage} from "@/lib/redux/slices/message/hasNewGroupMessageSlice";

const {Sider, Content} = Layout;
const {TextArea} = Input

export default function GroupChatContent({notificationApi}) {

    const dispatch = useDispatch()
    const username = useSelector(selectUsername)
    const hasNewGroupMessage = useSelector(selectHasNewGroupMessage)
    const [groupChatHeadListLoading, setGroupChatHeadListLoading] = useState(false)
    const [groupChatHeadTotalNumber, setGroupChatHeadTotalNumber] = useState(0)
    const [groupChatHeads, setGroupChatHeads] = useState([])
    const [selectedGroupChatId, setSelectedGroupChatId] = useState("")
    const [selectedGroupChatName, setSelectedGroupChatName] = useState("")
    const messageFormRef = useRef(null)
    const [sendMessageLoading, setSendMessageLoading] = useState(false)
    const [groupMessageTotalNumber, setGroupMessageTotalNumber] = useState(0)
    const [groupMessages, setGroupMessages] = useState([])

    useEffect(() => {
        loadGroupChatHeads()
    }, []);

    useEffect(() => {
        if (hasNewGroupMessage.hasNew) {
            const {groupChatId, groupMessageId} = hasNewGroupMessage
            const index = groupChatHeads.findIndex(groupChatHead => groupChatHead.groupChatId === groupChatId)
            if (index !== -1) {
                const groupChatHead = groupChatHeads[index]
                if (groupChatHead.groupChatId !== selectedGroupChatId) {
                    groupChatHead.readStatus = "UNREAD"
                }
                groupChatHeads.splice(index, 1)
                groupChatHeads.unshift(groupChatHead)
                setGroupChatHeads([...groupChatHeads])
            } else {
                axios.get(
                    "/api/group-chat/head/" + groupChatId
                ).then(resp => {
                    if (resp.data?.data) {
                        const {data: newGroupChatHead} = resp.data
                        const groupChatHead = {
                            ...newGroupChatHead,
                            id: newGroupChatHead.groupChatId,
                            title: newGroupChatHead.groupName
                        }
                        if (groupChatHead.groupChatId !== selectedGroupChatId) {
                            groupChatHead.readStatus = "UNREAD"
                        }
                        groupChatHeads.unshift(groupChatHead)
                        setGroupChatHeads([...groupChatHeads])
                    }
                }).catch(err => {
                    console.log(err)
                })
            }
            if (groupChatId === selectedGroupChatId) {
                axios.get(
                    "/api/group-chat/message/" + groupMessageId
                ).then(resp => {
                    if (resp.data?.data) {
                        const {data: newGroupMessage} = resp.data
                        groupMessages.unshift(newGroupMessage)
                        setGroupMessages([...groupMessages])
                    }
                }).catch(err => {
                    console.log(err)
                });
            }
            dispatch(clearHasNewGroupMessage())
        }
    }, [hasNewGroupMessage])

    const loadGroupChatHeads = () => {
        setGroupChatHeadListLoading(true)
        axios.get(
            "/api/group-chat/heads",
            {
                params: {
                    size: 30,
                    havingSize: groupChatHeads.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, groupChatHeads: newGroupChatHeads} = resp.data.data
                // console.log(newGroupChatHeads)
                setGroupChatHeadTotalNumber(total)
                if (groupChatHeads.length === 0) {
                    setGroupChatHeads(newGroupChatHeads.map(groupChatHead => {
                        return {
                            ...groupChatHead,
                            id: groupChatHead.groupChatId,
                            title: groupChatHead.groupName
                        }
                    }))
                } else {
                    const arr = newGroupChatHeads
                        .filter(groupChatHead =>
                            groupChatHeads.indexOf(curr => curr.groupChatId === groupChatHead.groupChatId) === -1)
                        .map(groupChatHead => {
                            return {
                                ...groupChatHead,
                                id: groupChatHead.groupChatId,
                                title: groupChatHead.groupName,
                                key: groupChatHead.groupChatId
                            }
                        })
                    arr.forEach(element => groupChatHeads.push(element))
                    setGroupChatHeads([...groupChatHeads])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Group Chat Error"
            })
        }).finally(() => {
            setGroupChatHeadListLoading(false)
        })
    }

    const loadMoreGroupChatHead = () => {
        if (groupChatHeads.length < groupChatHeadTotalNumber) {
            loadGroupChatHeads()
        }
    }

    const selectGroupChat = (id) => {
        if (id !== selectedGroupChatId) {
            groupMessages.splice(0, groupMessages.length)
            setGroupMessages([])
            setSelectedGroupChatId(id)
            const groupChatHead = groupChatHeads.find(groupChatHead => groupChatHead.groupChatId === id)
            groupChatHead.readStatus = "READ"
            setSelectedGroupChatName(groupChatHead.groupName)
            loadGroupMessages(id)
            messageFormRef.current?.resetFields()
        }
    }

    const loadGroupMessages = (groupChatId) => {
        // console.log("load group messages")
        axios.get(
            "/api/group-chat/messages",
            {
                params: {
                    groupChatId: groupChatId,
                    size: 30,
                    havingSize: groupMessages.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, groupMessages: newGroupMessages} = resp.data.data
                setGroupMessageTotalNumber(total)
                if (groupMessages.length === 0) {
                    setGroupMessages([...newGroupMessages])
                } else {
                    const arr = [...newGroupMessages]
                    arr.forEach(element => groupMessages.push(element))
                    setGroupMessages([...groupMessages])
                }
            }
        }).catch(err => {
            console.log(err)
        })
    }

    const loadMoreGroupMessages = () => {
        if (groupMessages.length < groupMessageTotalNumber) {
            loadGroupMessages(selectedGroupChatId)
        }
    }

    const sendMessage = (values) => {
        const {message} = values
        if (message?.trim().length > 0) {
            console.log(message)
            setSendMessageLoading(true)
            axios.post(
                "/api/group-chat/message",
                {
                    groupChatId: selectedGroupChatId,
                    content: message
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    messageFormRef.current?.resetFields();
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Fail to send message, Try again later"
                })
            }).finally(() => {
                setSendMessageLoading(false)
            })
        } else {
            notificationApi.warning({
                message: "Can not send empty message"
            })
            messageFormRef.current.resetFields()
        }
    }

    return (
        <>
            <Layout style={{height: "100%"}}>
                <Sider width={250} style={{backgroundColor: "white"}}>
                    <Card style={{height: "100%"}}>
                        <div
                            id="scrolableContactHeads"
                            style={{
                                height: "75vh",
                                overflow: "auto",
                            }}
                        >
                            <InfiniteScroll
                                next={loadMoreGroupChatHead}
                                hasMore={groupChatHeads.length < groupChatHeadTotalNumber}
                                loader={null}
                                dataLength={groupChatHeads.length}
                                endMessage={<Divider plain>No More Group Chats</Divider>}
                                scrollableTarget="scrolableContactHeads"
                            >
                                <List
                                    loading={groupChatHeadListLoading}
                                    locale={{emptyText: <span></span>}}
                                >
                                    <ListItemsRender
                                        items={groupChatHeads}
                                        initialId={selectedGroupChatId}
                                        itemOnSelect={selectGroupChat}
                                    />
                                </List>
                            </InfiniteScroll>
                        </div>
                    </Card>
                </Sider>
                <Content style={{backgroundColor: "white"}}>
                    <Card
                        title={<h3>{selectedGroupChatName}</h3>}
                        style={{height: "100%"}}
                    >
                        {
                            selectedGroupChatId === "" ?
                                <>
                                    <h1>Select a group chat and start to chat with group mates</h1>
                                </> :
                                <>
                                    <Space
                                        direction="vertical"
                                        style={{width: "100%"}}
                                    >
                                        <div
                                            id="scrolableGroupMessages"
                                            style={{
                                                height: "40vh",
                                                overflow: "auto",
                                                borderColor: "gray", borderStyle: "solid",
                                                borderRadius: "12px", borderWidth: "thin",
                                                display: 'flex',
                                                flexDirection: 'column-reverse',
                                            }}
                                        >
                                            <InfiniteScroll
                                                next={loadMoreGroupMessages}
                                                hasMore={groupMessages.length < groupMessageTotalNumber}
                                                loader="Loading..."
                                                dataLength={groupMessages.length}
                                                scrollableTarget="scrolableGroupMessages"
                                                style={{display: 'flex', flexDirection: 'column-reverse'}}
                                            >
                                                {groupMessages.map(groupMessage => (
                                                    <div
                                                        key={groupMessage.id}
                                                        style={{
                                                            display: "flex", flexDirection: "column",
                                                            textAlign: groupMessage.username === username ? "end" : "left",
                                                            marginTop: "10px"
                                                        }}
                                                    >
                                                        {
                                                            groupMessage.username === username ?
                                                                <span style={{color: "navy"}}>My Message</span> :
                                                                <span
                                                                    style={{color: "orange"}}>{groupMessage.username}</span>
                                                        }
                                                        <span>{groupMessage.createdTime}</span>
                                                        <span>{groupMessage.content}</span>
                                                        <Divider style={{margin: "0 0 0 0"}}/>
                                                    </div>
                                                ))}
                                            </InfiniteScroll>
                                        </div>
                                        <Divider/>
                                        <Form
                                            name="messageForm"
                                            ref={messageFormRef}
                                            autoComplete="off"
                                            onFinish={sendMessage}
                                        >
                                            <Form.Item
                                                name="message"
                                            >
                                                <TextArea allowClear={true}/>
                                            </Form.Item>
                                            <Form.Item
                                                style={{
                                                    display: "flex",
                                                    justifyContent: "flex-end"
                                                }}
                                            >
                                                <Button type="primary" htmlType="submit" loading={sendMessageLoading}>
                                                    Send Message
                                                </Button>
                                            </Form.Item>
                                        </Form>
                                    </Space>
                                </>
                        }
                    </Card>
                </Content>
            </Layout>
        </>
    )
}