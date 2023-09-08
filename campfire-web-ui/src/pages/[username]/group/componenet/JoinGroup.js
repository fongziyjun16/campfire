import {Button, Card, Divider, Form, Input, List, Modal, Space, Tabs, Tooltip} from "antd";
import InfiniteScroll from 'react-infinite-scroll-component';
import {useState} from "react";
import axios from "axios";
import {BiRefresh} from "react-icons/bi";

const {TextArea} = Input

export default function JoinGroup({notificationApi}) {

    const [searchingName, setSearchingName] = useState("")
    const [notJoinInGroupsLoading, setNotJoinInGroupsLoading] = useState(false)
    const [notJoinInGroupTotalNumber, setNotJoinInGroupTotalNumber] = useState(0)
    const [notJoinInGroups, setNotJoinInGroups] = useState([])
    const [selectedNotJoinInGroup, setSelectedNotJoinInGroup] = useState(null)
    const [isRequestJoinInModalOpen, setIsRequestJoinInModalOpen] = useState(false)
    const [requestingJoinIn, setRequestingJoinIn] = useState(false)
    const [waitingJoinInGroupsLoading, setWaitingJoinInGroupsLoading] = useState(false)
    const [waitingJoinInGroupTotalNumber, setWaitingJoinInGroupTotalNumber] = useState(0)
    const [waitingJoinInGroups, setWaitingJoinInGroups] = useState([])

    const clearNotJoinInGroups = () => {
        setNotJoinInGroupTotalNumber(0)
        notJoinInGroups.splice(0, notJoinInGroups.length)
        setNotJoinInGroups([...notJoinInGroups])
    }

    const clearWaitingJoinInGroups = () => {
        setWaitingJoinInGroupTotalNumber(0)
        waitingJoinInGroups.splice(0, waitingJoinInGroups.length)
        setWaitingJoinInGroups([...waitingJoinInGroups])
    }

    const searchGroup = (values) => {
        const {searchingName} = values
        if (searchingName && searchingName.trim().length > 0) {
            setSearchingName(searchingName)
            clearNotJoinInGroups()
            loadNotJoinInGroups(searchingName)
        } else {
            notificationApi.warning({
                message: "Group Name cannot be empty"
            })
        }
    }

    const loadNotJoinInGroups = (searchingName) => {
        setNotJoinInGroupsLoading(true)
        axios.get(
            "/api/group/not-join-in-groups",
            {
                params: {
                    searchingName: searchingName,
                    size: 20,
                    havingSize: notJoinInGroups.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items: gs} = resp.data.data
                setNotJoinInGroupTotalNumber(total)
                if (gs?.length > 0) {
                    if (notJoinInGroups.length === 0) {
                        setNotJoinInGroups([...gs]);
                    } else {
                        const arr = [...gs];
                        arr.forEach(element => notJoinInGroups.push(element));
                        setNotJoinInGroups([...notJoinInGroups]);
                    }
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Groups Not Join In Error. Try again later"
            })
        }).finally(() => {
            setNotJoinInGroupsLoading(false)
        })
    }

    const loadMoreNotJoinInGroups = () => {
        if (notJoinInGroups.length < notJoinInGroupTotalNumber) {
            loadNotJoinInGroups(searchingName);
        }
    }

    const requestJoinIn = (values) => {
        const {comment} = values
        if (comment?.trim().length > 0) {
            console.log(selectedNotJoinInGroup)
            setRequestingJoinIn(true)
            axios.post(
                "/api/group/apply-to-join",
                {
                    groupId: selectedNotJoinInGroup.id,
                    comment: comment
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    setIsRequestJoinInModalOpen(false)
                    clearNotJoinInGroups()
                    notificationApi.success({
                        message: "Send out request",
                    })
                } else {
                    notificationApi.error({
                        message: "Request Join In Error.",
                        description: "Wrong Parameters"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Request Join In Error. Try again later"
                })
            }).finally(() => {
                setRequestingJoinIn(false)
            })
        } else {
            notificationApi.warning({
                message: "Request Join In Message CANNOT BE EMPTY"
            })
        }
    }

    const loadWaitingJoinInGroups = () => {
        setWaitingJoinInGroupsLoading(true)
        axios.get(
            "/api/group/waiting-join-in-groups",
            {
                params: {
                    size: 20,
                    havingSize: waitingJoinInGroups.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setWaitingJoinInGroupTotalNumber(total)
                if (waitingJoinInGroups.length === 0) {
                    setWaitingJoinInGroups([...items])
                } else {
                    const arr = [...items]
                    arr.forEach(e => waitingJoinInGroups.push(e))
                    setWaitingJoinInGroups([...waitingJoinInGroups])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Waiting Join In Groups Error. Try again later"
            })
        }).finally(() => {
            setWaitingJoinInGroupsLoading(false)
        })
    }

    const loadMoreWaitingJoinInGroups = () => {
        if (waitingJoinInGroups.length < waitingJoinInGroupTotalNumber) {
            loadWaitingJoinInGroups()
        }
    }

    return (
        <>
            <Card title="Join New Group" style={{height: "100%", overflow: "auto"}}>
                <Tabs
                    tabPosition="left"
                    items={[
                        {
                            key: "searchOrJoin",
                            label: "Search / Join",
                            children: <>
                                <Space direction="vertical" style={{width: "100%"}}>
                                    <Form
                                        layout="inline"
                                        autoComplete="off"
                                        onFinish={searchGroup}
                                    >
                                        <Form.Item name="searchingName">
                                            <Input placeholder="group name" allowClear/>
                                        </Form.Item>
                                        <Form.Item>
                                            <Button type="primary" htmlType="submit">
                                                Search
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                    <div
                                        style={{
                                            height: "75vh", overflow: "auto",
                                            border: "1px solid black", borderRadius: "5px"
                                        }}
                                    >
                                        <List
                                            loading={notJoinInGroupsLoading}
                                            locale={{emptyText: <span></span>}}
                                            dataSource={notJoinInGroups}
                                            renderItem={notJoinInGroup => (
                                                <List.Item
                                                    key={notJoinInGroup.id}
                                                    actions={[
                                                        <Tooltip
                                                            key={notJoinInGroup.id + "_description"}
                                                            title={notJoinInGroup.description}
                                                            placement="bottom"
                                                        >
                                                            <Button>Description</Button>
                                                        </Tooltip>,
                                                        <Button
                                                            key={notJoinInGroup.id + "_request_join_in_btn"}
                                                            type="primary"
                                                            onClick={() => {
                                                                setSelectedNotJoinInGroup(notJoinInGroup)
                                                                setIsRequestJoinInModalOpen(true)
                                                            }}
                                                        >
                                                            Request Join In
                                                        </Button>
                                                    ]}
                                                >
                                                    <List.Item.Meta title={notJoinInGroup.name}/>
                                                </List.Item>
                                            )}
                                            loadMore={
                                                <div
                                                    style={{
                                                        textAlign: 'center',
                                                        marginTop: 12,
                                                        height: 32,
                                                        lineHeight: '32px',
                                                    }}
                                                >
                                                    {
                                                        notJoinInGroups.length < notJoinInGroupTotalNumber ?
                                                            <Button
                                                                loading={notJoinInGroupsLoading}
                                                                onClick={() => loadMoreNotJoinInGroups()}
                                                            >
                                                                load more
                                                            </Button> :
                                                            <span>------ Not More Searching Groups ------</span>
                                                    }
                                                </div>
                                            }
                                        />
                                    </div>
                                </Space>
                            </>
                        },
                        {
                            key: "waiting",
                            label: "Waiting",
                            children: <>
                                <Space direction="vertical" style={{width: "100%"}}>
                                    <Button
                                        icon={<BiRefresh/>}
                                        onClick={() => {
                                            clearWaitingJoinInGroups()
                                            loadWaitingJoinInGroups()
                                        }}
                                    />
                                    <div
                                        style={{
                                            height: "75vh", overflow: "auto",
                                            border: "1px solid black", borderRadius: "5px"
                                        }}
                                    >
                                        <List
                                            loading={waitingJoinInGroupsLoading}
                                            locale={{emptyText: <span></span>}}
                                            dataSource={waitingJoinInGroups}
                                            renderItem={waitingJoinInGroup => (
                                                <List.Item
                                                    actions={[
                                                        <Tooltip
                                                            key={waitingJoinInGroup.id + "_message"}
                                                            title={waitingJoinInGroup.comment}
                                                        >
                                                            <Button>Message</Button>
                                                        </Tooltip>
                                                    ]}
                                                >
                                                    <List.Item.Meta
                                                        title={waitingJoinInGroup.name}
                                                        description={waitingJoinInGroup.joinTime}
                                                    />
                                                </List.Item>
                                            )}
                                            loadMore={
                                                <div
                                                    style={{
                                                        textAlign: 'center',
                                                        marginTop: 12,
                                                        height: 32,
                                                        lineHeight: '32px',
                                                    }}
                                                >
                                                    {
                                                        waitingJoinInGroups.length < waitingJoinInGroupTotalNumber ?
                                                            <Button
                                                                loading={waitingJoinInGroupsLoading}
                                                                onClick={() => loadMoreWaitingJoinInGroups()}
                                                            >
                                                                load more
                                                            </Button> :
                                                            <span>------ Not More Searching Groups ------</span>
                                                    }
                                                </div>
                                            }
                                        />
                                    </div>
                                </Space>
                            </>
                        }
                    ]}
                    onChange={(activeKey) => {
                        if (activeKey === "waiting") {
                            clearWaitingJoinInGroups()
                            loadWaitingJoinInGroups()
                        }
                    }}
                />
            </Card>
            <Modal
                open={isRequestJoinInModalOpen}
                title={selectedNotJoinInGroup?.name}
                footer={null}
                destroyOnClose={true}
                onCancel={() => setIsRequestJoinInModalOpen(false)}
            >
                <Form
                    autoComplete="off"
                    onFinish={requestJoinIn}
                >
                    <Form.Item name="comment">
                        <TextArea placeholder="leave your message"/>
                    </Form.Item>
                    <Form.Item style={{display: "flex", justifyContent: "flex-end"}}>
                        <Button type="primary" htmlType="submit" loading={requestingJoinIn}>
                            Request Join In
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    )
}