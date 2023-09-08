import {Button, Card, Divider, Form, Input, Layout, List, Space, Spin, Tooltip, Typography} from "antd";
import InfiniteScroll from 'react-infinite-scroll-component';
import {useEffect, useRef, useState} from "react";
import ListItemsRender from "@/pages/[username]/message/component/content/ListItemsRender";
import axios from "axios";
import {useDispatch, useSelector} from "react-redux";
import {
    clearHasNewContactMessage,
    selectHasNewContactMessage
} from "@/lib/redux/slices/message/hasNewContactMessageSlice";

const {Sider, Content} = Layout;
const {TextArea} = Input

export default function ContactMessageContent({notificationApi}) {

    const dispatch = useDispatch()
    const hasNewContactMessage = useSelector(selectHasNewContactMessage)
    const [contactHeadListLoading, setContactHeadListLoading] = useState(false)
    const [contactTotalNumber, setContactTotalNumber] = useState(0)
    const [contactHeads, setContactHeads] = useState([])
    const [selectedContactId, setSelectedContactId] = useState("")
    const [selectedTargetId, setSelectedTargetId] = useState("0")
    const [selectedTargetUsername, setSelectedTargetUsername] = useState("")
    const messageFormRef = useRef(null)
    const [contactMessagesLoading, setContactMessageLoading] = useState(false)
    const [contactMessageTotalNumber, setContactMessageTotalNumber] = useState(0)
    const [contactMessages, setContactMessages] = useState([])
    const [sendMessageLoading, setSendMessageLoading] = useState(false)

    useEffect(() => {
        loadFirstBatchOfContactHeads()
    }, []);

    useEffect(() => {
        if (hasNewContactMessage.hasNew) {
            // console.log(hasNewContactMessage)
            const {id: contactHeadId, contactMessageId} = hasNewContactMessage
            const index = contactHeads.findIndex(contactHead => contactHead.id === contactHeadId)
            if (index !== -1) {
                const contactHead = contactHeads[index]
                if (contactHead.id !== selectedContactId) {
                    contactHead.readStatus = "UNREAD";
                }
                contactHeads.splice(index, 1)
                contactHeads.unshift(contactHead)
                // console.log(contactHead)
                setContactHeads([...contactHeads])
            } else {
                axios.get(
                    "/api/contact-chat/contact-head/" + contactHeadId
                ).then(resp => {
                    if (resp.data) {
                        const {data: contactChatHead} = resp.data
                        // console.log(contactHead)
                        const contactHead = {
                            ...contactChatHead,
                            title: contactChatHead.targetUsername
                        }
                        if (contactHead.id !== selectedContactId) {
                            contactHead.readStatus = "UNREAD";
                        }
                        contactHeads.unshift(contactHead)
                        setContactHeads([...contactHeads]);
                    }
                }).catch(err => {
                    console.log(err)
                })
            }
            if (contactHeadId === selectedContactId) {
                axios.get(
                    "/api/contact-chat/contact-message/" + contactMessageId
                ).then(resp => {
                    if (resp.data?.data) {
                        const {data: newContactMessage} = resp.data
                        // console.log(newContactMessage)
                        contactMessages.unshift(newContactMessage)
                        setContactMessages([...contactMessages])
                    }
                }).catch(err => {
                    console.log(err)
                })
            }
            dispatch(clearHasNewContactMessage())
        }
    }, [hasNewContactMessage]);

    const loadFirstBatchOfContactHeads = () => {
        setContactHeadListLoading(true)
        axios.get(
            "/api/contact-chat/first-batch-contact-heads/" + 30
        ).then(resp => {
            if (resp.data) {
                const {total, contactChatHeads} = resp.data.data
                // console.log(contactChatHeads)
                setContactTotalNumber(total)
                setContactHeads(contactChatHeads.map(contactChatHead => {
                    return {
                        ...contactChatHead,
                        title: contactChatHead.targetUsername
                    }
                }))
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Contacts Error. Try again later"
            })
        }).finally(() => {
            setContactHeadListLoading(false)
        })
    }

    const loadMoreContactHeads = () => {
        if (contactHeads.length <= contactTotalNumber) {
            axios.get(
                "/api/contact-chat/more-contact-heads",
                {
                    params: {
                        size: 30,
                        havingSize: contactHeads.length
                    }
                }
            ).then(resp => {
                if (resp.data) {
                    const {total, contactChatHeads: newContactChatHeads} = resp.data.data
                    // console.log(newContactChatHeads)
                    setContactTotalNumber(total)
                    for (let i = 0; i < newContactChatHeads.length; i++) {
                        if (contactHeads.indexOf(contactHead => contactHead.id === newContactChatHeads[i].id) === -1) {
                            contactHeads.push(newContactChatHeads[i])
                        }
                    }
                    setContactHeads(contactHeads.map(contactHead => {
                        return {
                            ...contactHead,
                            title: contactHead.targetUsername
                        }
                    }))
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Load More Contacts Error. Try again later"
                })
            });
        }
    }

    const selectContact = (id) => {
        if (id !== selectedContactId) {
            const contactHead = contactHeads.find(contactHead => contactHead.id === id)
            contactHead.readStatus = "READ"
            setSelectedContactId(id)
            setSelectedTargetId(contactHead.targetId)
            setSelectedTargetUsername(contactHead.targetUsername)
            loadFirstBatchOfContactMessages(id)
            messageFormRef.current?.resetFields()
        }
    }

    const loadFirstBatchOfContactMessages = (id) => {
        const contactHead = contactHeads.find(contactHead => contactHead.id === id)
        setContactMessageLoading(true)
        axios.get(
            "/api/contact-chat/first-batch-contact-messages",
            {
                params: {
                    "targetId": contactHead.targetId,
                    "size": 30
                }
            }
        ).then(resp => {
            // console.log(resp)
            if (resp.data?.data) {
                const {total, contactMessages: newContactMessages} = resp.data.data
                setContactMessageTotalNumber(total)
                setContactMessages([...newContactMessages])
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "No more contact messages in this contact. Try again Later"
            })
        }).finally(() => {
            setContactMessageLoading(false)
        })
    }

    const loadMoreContactMessages = () => {
        if (contactMessages.length < contactMessageTotalNumber) {
            axios.get(
                "/api/contact-chat/more-contact-messages",
                {
                    params: {
                        targetId: selectedTargetId,
                        size: 30,
                        havingSize: contactMessages.length
                    }
                }
            ).then(resp => {
                if (resp.data?.data) {
                    const {total, contactMessages: newContactMessages} = resp.data.data
                    setContactMessageTotalNumber(total)
                    contactMessages.push(...newContactMessages)
                    // console.log(contactMessages)
                    setContactMessages([...contactMessages])
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Cannot load more contact messages in this contact. Try again Later"
                })
            })
        }
    }

    const sendMessage = (values) => {
        const {message} = values
        if (message?.trim().length > 0) {
            setSendMessageLoading(true)
            // console.log(message)
            axios.post(
                "/api/contact-chat/message",
                {
                    targetId: selectedTargetId,
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
                    message: "Send message error. Try again later"
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
                <Sider
                    width={250}
                    style={{backgroundColor: "white"}}
                >
                    <Card style={{height: "100%"}}>
                        <div
                            id="scrolableContactHeads"
                            style={{
                                height: "75vh",
                                overflow: "auto",
                            }}
                        >
                            <InfiniteScroll
                                next={loadMoreContactHeads}
                                hasMore={contactHeads.length < contactTotalNumber}
                                loader={null}
                                dataLength={contactHeads.length}
                                endMessage={<Divider plain>No More Contacts</Divider>}
                                scrollableTarget="scrolableContactHeads"
                            >
                                <List
                                    loading={contactHeadListLoading}
                                    locale={{emptyText: <span></span>}}
                                >
                                    <ListItemsRender
                                        items={contactHeads}
                                        initialId={selectedContactId}
                                        itemOnSelect={selectContact}
                                    />
                                </List>
                            </InfiniteScroll>
                        </div>
                    </Card>
                </Sider>
                <Content style={{backgroundColor: "white"}}>
                    <Card
                        title={<h3>{selectedTargetUsername}</h3>}
                        style={{height: "100%", overflow: "auto"}}
                    >
                        {
                            selectedContactId === "" ?
                                <>
                                    <h1>Select a contact and start to chat</h1>
                                </> :
                                <>
                                    <Space
                                        direction="vertical"
                                        style={{width: "100%"}}
                                    >
                                        <div
                                            id="scrolableContactMessages"
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
                                                next={loadMoreContactMessages}
                                                hasMore={contactMessages.length < contactMessageTotalNumber}
                                                loader="Loading......"
                                                dataLength={contactMessages.length}
                                                inverse={true}
                                                scrollableTarget="scrolableContactMessages"
                                                style={{display: 'flex', flexDirection: 'column-reverse'}}
                                            >
                                                {contactMessages.map(contactMessage => (
                                                    <div
                                                        key={contactMessage.id}
                                                        style={{
                                                            display: "flex", flexDirection: "column",
                                                            textAlign: contactMessage.creatorId === selectedTargetId ? "left" : "end",
                                                            marginTop: "10px"
                                                        }}
                                                    >
                                                        {
                                                            contactMessage.creatorId === selectedTargetId ?
                                                                <span style={{color: "orange"}}>{selectedTargetUsername}</span> :
                                                                <span style={{color: "navy"}}>My Message</span>
                                                        }
                                                        <span>{contactMessage.createdTime}</span>
                                                        <span>{contactMessage.content}</span>
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