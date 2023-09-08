import {Card, Divider, Layout, List, Typography} from "antd";
import InfiniteScroll from 'react-infinite-scroll-component';
import {useEffect, useState} from "react";
import axios from "axios";
import ListItemsRender from "@/pages/[username]/message/component/content/ListItemsRender";
import {useDispatch, useSelector} from "react-redux";
import {
    clearHasNewPublicNotification,
    selectHasNewPublicNotification,
} from "@/lib/redux/slices/message/hasNewPublicNotificationSlice";
import {
    clearHasNewPrivateNotification,
    selectHasNewPrivateNotification
} from "@/lib/redux/slices/message/hasNewPrivateNotificationSlice";

const {Sider, Content} = Layout;
const {Title, Paragraph} = Typography

export default function NotificationContent({notificationApi}) {

    const dispatch = useDispatch()
    const hasNewPublicNotification = useSelector(selectHasNewPublicNotification)
    const hasNewPrivateNotification = useSelector(selectHasNewPrivateNotification)
    const [loading, setLoading] = useState(true)
    const [contentLoading, setContentLoading] = useState(false)
    const [pageNo, setPageNo] = useState(1)
    const [pageSize, setPageSize] = useState(20)
    const [total, setTotal] = useState(0)
    const [totalPage, setTotalPage] = useState(0)
    const [selectedNotificationId, setSelectedNotificationId] = useState(0)
    const [notificationHeads, setNotificationHeads] = useState([])
    const [title, setTitle] = useState("")
    const [content, setContent] = useState("")

    useEffect(() => {
        loadFirstBatchNotificationHeads()
    }, []);

    useEffect(() => {
        if (hasNewPublicNotification.hasNew) {
            axios.get(
                "/api/notification/head/" + hasNewPublicNotification.id
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    const {data: notificationHead} = resp.data
                    notificationHeads.unshift(notificationHead)
                    setNotificationHeads([...notificationHeads])
                    dispatch(clearHasNewPublicNotification())
                }
            }).catch(err => {
                console.log(err)
            });
        }
    }, [hasNewPublicNotification])

    useEffect(() => {
        if (hasNewPrivateNotification.hasNew) {
            axios.get(
                "/api/notification/head/" + hasNewPrivateNotification.id
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    const {data: notificationHead} = resp.data
                    notificationHeads.unshift(notificationHead)
                    setNotificationHeads([...notificationHeads])
                    dispatch(clearHasNewPrivateNotification())
                }
            }).catch(err => {
                console.log(err)
            });
        }
    }, [hasNewPrivateNotification])

    const loadFirstBatchNotificationHeads = () => {
        axios.get(
            "/api/notification/heads",
            {
                params: {
                    pageNo: pageNo,
                    pageSize: pageSize
                }
            }
        ).then(resp => {
            if (resp !== null && resp.data.code === 200) {
                // console.log(resp.data)
                const {pageNo, pageSize, total, totalPage, notificationHeads} = resp.data.data
                setTotal(total)
                setTotalPage(totalPage)
                setNotificationHeads([...notificationHeads])
            } else {
                notificationApi.warning({
                    message: "No Notifications"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Request Notifications Error. Try later"
            })
        }).finally(() => {
            setLoading(false)
        })
    }

    const loadMoreNotificationHeads = () => {
        // console.log("load more")
        if (notificationHeads.length <= total) {
            setLoading(true)
            axios.get(
                "/api/notification/more-heads",
                {
                    params: {
                        size: pageSize,
                        afterNotificationId: notificationHeads[notificationHeads.length - 1].id
                    }
                }
            ).then(resp => {
                if (resp !== null && resp.data.code === 200) {
                    const {pageNo, pageSize, total, totalPage, notificationHeads: newNotificationHeads} = resp.data.data
                    setTotal(total)
                    setNotificationHeads([...notificationHeads.concat(newNotificationHeads)])
                } else {
                    notificationApi.warning({
                        message: "No Notifications"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Request More Notifications Error. Try later"
                })
            }).finally(() => {
                setLoading(false)
            })
        }
    }

    const selectNotification = (id, type) => {
        // console.log(type)
        const notificationHead = notificationHeads.find(notificationHead => notificationHead.id === id)
        notificationHead.readStatus = "READ"
        setNotificationHeads([...notificationHeads])
        setSelectedNotificationId(id)
        setContentLoading(true)
        axios.get(
            "/api/notification/" + type.toLowerCase() + "/" + id
        ).then(resp => {
            if (resp.data && resp.data.code === 200) {
                const {title, content} = resp.data.data
                setTitle(title)
                setContent(content)
            } else {
                notificationApi.warning({
                    message: "Notification Not Found"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Read Notification Error, Try again later"
            })
        }).finally(() => {
            setContentLoading(false)
        })
    }

    return (
        <>
            <Layout style={{height: "100%"}}>
                <Sider style={{backgroundColor: "white"}} width={300}>
                    <Card style={{height: "100%"}}>
                        <div
                            id="scrolableDiv"
                            style={{
                                height: "80vh",
                                overflow: "auto",
                            }}
                        >
                            <InfiniteScroll
                                next={loadMoreNotificationHeads}
                                hasMore={notificationHeads.length < total}
                                loader={null}
                                dataLength={notificationHeads.length}
                                endMessage={<Divider plain>No More Notifications</Divider>}
                                scrollableTarget="scrolableDiv"
                            >
                                <List
                                    loading={loading}
                                    locale={{emptyText: <span></span>}}
                                >
                                    <ListItemsRender
                                        items={notificationHeads}
                                        initialId={selectedNotificationId}
                                        itemOnSelect={selectNotification}
                                    />
                                </List>
                            </InfiniteScroll>
                        </div>
                    </Card>
                </Sider>
                <Content style={{backgroundColor: "white"}}>
                    <Card
                        loading={contentLoading}
                        style={{height: "100%", overflow: "auto"}}
                    >
                        <Typography>
                            <Title>{title ? title : "Select a notification to check"}</Title>
                            <Paragraph>{content}</Paragraph>
                        </Typography>
                    </Card>
                </Content>
            </Layout>
        </>
    )
}