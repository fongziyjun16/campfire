import {useEffect, useState} from "react";
import {useSelector} from "react-redux";
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";
import {Button, Col, Divider, Form, Input, List, Popconfirm, Row, Space, Tabs, Tooltip} from "antd";
import {BiRefresh} from "react-icons/bi";
import axios from "axios";

export default function MemberTab({notificationApi, groupId, leaderId}) {

    const accountId = useSelector(selectAccountId)

    return (
        <>
            <Tabs
                tabPosition="left"
                items={(() => {
                    const items = [
                        {
                            key: "member",
                            label: "Member",
                            children: <Member notificationApi={notificationApi} groupId={groupId} leaderId={leaderId}/>
                        }
                    ]
                    if (accountId === leaderId) {
                        items.push({
                            key: "joinRequest",
                            label: "Join Request",
                            children: <JoinRequest notificationApi={notificationApi} groupId={groupId}
                                                   leaderId={leaderId}/>
                        })
                    }
                    return items;
                })()}
                style={{width: "98%"}}
            />
        </>
    )
}

function Member({notificationApi, groupId, leaderId}) {

    const account = useSelector(selectAccountId)
    const [membersLoading, setMemberLoading] = useState(false)
    const [memberTotalNumber, setMemberTotalNumber] = useState(0)
    const [members, setMembers] = useState([])
    const [removingMember, setRemovingMember] = useState(false)
    const [transferringLeader, setTransferringLeader] = useState(false)
    const [transferLeaderForm] = Form.useForm()
    const [dismissingGroup, setDismissingGroup] = useState(false)
    const [leavingGroup, setLeavingGroup] = useState(false)

    useEffect(() => {
        loadMembers()
    }, []);

    const clearMembers = () => {
        setMemberTotalNumber(0)
        members.splice(0, members.length)
        setMembers([...members])
    }

    const loadMembers = () => {
        setMemberLoading(true)
        axios.get(
            "/api/group/members",
            {
                params: {
                    groupId: groupId,
                    size: 10,
                    havingSize: members.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setMemberTotalNumber(total)
                if (items?.length > 0) {
                    if (members.length === 0) {
                        setMembers([...items]);
                    } else {
                        const arr = [...items];
                        arr.forEach(e => members.push(e));
                        setMembers([...members]);
                    }
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Group Members Error. Try again later"
            })
        }).finally(() => {
            setMemberLoading(false)
        })
    }

    const loadMoreMembers = () => {
        // console.log("load more")
        if (members.length < memberTotalNumber) {
            loadMembers()
        }
    }

    const removeMember = (joining) => {
        // console.log(joining)
        setRemovingMember(true)
        axios.delete(
            "/api/group/member",
            {
                params: {
                    accountId: joining.accountId,
                    groupId: groupId
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Remove Member Error Successfully."
                })
                clearMembers()
                loadMembers()
            } else {
                notificationApi.error({
                    message: "Remove Member Error.",
                    description: "Operations Not Allowed"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Remove Member Error. Try again later"
            })
        }).finally(() => {
            setRemovingMember(false)
        })
    }

    const transferLeader = (values) => {
        // console.log(values)
        const {targetUser} = values
        if (targetUser?.trim().length > 0) {
            const member = members.find(e => e.username === targetUser)
            if (member) {
                // console.log(member)
                setTransferringLeader(true);
                axios.put(
                    "/api/group/transfer-leader",
                    {
                        accountId: member.accountId,
                        groupId: groupId
                    },
                    {
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        }
                    }
                ).then(resp => {
                    if (resp.data?.code === 200) {
                        notificationApi.success({
                            message: "Transferring Leader Successfully.",
                        })
                        location.reload()
                    } else {
                        notificationApi.error({
                            message: "Transferring Leader Error.",
                            description: "Insufficient Information or Wrong Status"
                        })
                    }
                }).catch(err => {
                    console.log(err)
                    notificationApi.error({
                        message: "Transferring Leader Error. Try again later"
                    })
                }).finally(() => {
                    setTransferringLeader(false)
                })
            }
        } else {
            notificationApi.warning({
                message: "Must input a username for transferring leader"
            })
        }
    }

    const dismissGroup = () => {
        setDismissingGroup(true)
        axios.delete(
            "/api/group/dismiss",
            {
                params: {
                    groupId: groupId
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Dismiss Group Successfully.",
                })
                location.reload()
            } else {
                notificationApi.error({
                    message: "Dismiss Group Error.",
                    description: "Insufficient Information or Wrong Status"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Dismiss Group Error. Try again later"
            })
        }).finally(() => {
            setDismissingGroup(false)
        })
    }

    const leaveGroup = () => {
        setLeavingGroup(true)
        axios.delete(
            "/api/group/leave",
            {
                params: {
                    groupId: groupId
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Leave Group Successfully"
                })
                location.reload()
            } else {
                notificationApi.error({
                    message: "Leave Group Error.",
                    description: "Insufficient Information or Wrong Status"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Leave Group Error. Try again later"
            })
        }).finally(() => {
            setLeavingGroup(false)
        })
    }

    return (
        <>
            <Space direction="vertical" style={{width: "100%"}}>
                <Row>
                    <Col span={12}>
                        <Button
                            icon={<BiRefresh/>}
                            onClick={() => {
                                clearMembers()
                                loadMembers()
                            }}
                        />
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        The Number of Members: {memberTotalNumber}
                    </Col>
                </Row>
                <div
                    style={{
                        height: "75vh", overflow: "auto",
                        border: "1px solid black", borderRadius: "5px"
                    }}
                >
                    <List
                        loading={membersLoading}
                        locale={{emptyText: <span></span>}}
                        dataSource={members}
                        renderItem={item => (
                            <List.Item
                                key={item.joiningId}
                                actions={(() => {
                                    const arr = []
                                    if (account === leaderId) {
                                        arr.push(
                                            <Popconfirm
                                                key={item.joiningId + "_remove_btn"}
                                                title="Remove the member"
                                                description="Are you sure to remove this member?"
                                                onConfirm={() => removeMember(item)}
                                                okText="Yes"
                                                cancelText="No"
                                            >
                                                <Button type="primary" danger loading={removingMember}>Remove</Button>
                                            </Popconfirm>
                                        )
                                    }
                                    return arr;
                                })()}
                            >
                                <List.Item.Meta title={item.username} description={item.role + " - " + item.joinTime}/>
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
                                    members.length < memberTotalNumber ?
                                        <Button
                                            loading={membersLoading}
                                            onClick={() => loadMoreMembers()}
                                        >
                                            load more
                                        </Button> :
                                        <span>------ Not More Group Members------</span>
                                }
                            </div>
                        }
                    />
                </div>
                {
                    account === leaderId ?
                        <>
                            <Divider/>
                            <Form
                                layout="inline"
                                autoComplete="off"
                                form={transferLeaderForm}
                                onFinish={transferLeader}
                            >
                                <Form.Item
                                    name="targetUser"
                                    label="Transfer Leader to"
                                >
                                    <Input placeholder="member username"/>
                                </Form.Item>
                                <Form.Item>
                                    <Popconfirm
                                        title="Transfer Leader"
                                        description="Are you sure to transfer this leader to this member?"
                                        onConfirm={() => transferLeaderForm.submit()}
                                        okText="Yes"
                                        cancelText="No"
                                    >
                                        <Button type="primary" loading={transferringLeader}>
                                            Confirm
                                        </Button>
                                    </Popconfirm>
                                </Form.Item>
                            </Form>
                        </> : <></>
                }
                <Divider/>
                <Row>
                    <Col span={12}>
                        {
                            account === leaderId ?
                                <Popconfirm
                                    title="Dismiss this group"
                                    description="Are you sure to dismiss this group?"
                                    onConfirm={dismissGroup}
                                    okText="Yes"
                                    cancelText="No"
                                >
                                    <Button type="primary" danger loading={dismissingGroup}>
                                        Dismiss This Group
                                    </Button>
                                </Popconfirm> : <></>
                        }
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        {
                            account !== leaderId ?
                                <Popconfirm
                                    title="Leave this group"
                                    description="Are you sure to leave this group?"
                                    onConfirm={leaveGroup}
                                    okText="Yes"
                                    cancelText="No"
                                >
                                    <Button type="primary" danger loading={leavingGroup}>
                                        Leave This Group
                                    </Button>
                                </Popconfirm> : <></>
                        }
                    </Col>
                </Row>
            </Space>
        </>
    )
}

function JoinRequest({notificationApi, groupId, leaderId}) {

    const [joiningRequestsLoading, setJoiningRequestsLoading] = useState(false)
    const [joiningRequestTotalNumber, setJoiningRequestTotalNumber] = useState(0)
    const [joiningRequests, setJoiningRequests] = useState([])
    const [requestProcessing, setRequestProcessing] = useState(false)

    useEffect(() => {
        loadJoiningRequests()
    }, []);

    const clearJoiningRequests = () => {
        setJoiningRequestTotalNumber(0)
        joiningRequests.splice(0, joiningRequests.length)
        setJoiningRequests([...joiningRequests])
    }

    const loadJoiningRequests = () => {
        setJoiningRequestsLoading(true)
        axios.get(
            "/api/group/join-in-requests",
            {
                params: {
                    groupId: groupId,
                    size: 20,
                    havingSize: joiningRequests.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setJoiningRequestTotalNumber(total)
                if (joiningRequests.length === 0) {
                    setJoiningRequests([...items])
                } else {
                    const arr = [...items]
                    arr.forEach(e => joiningRequests.push(e))
                    setJoiningRequests([...joiningRequests])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Join In Request Error. Try again later"
            })
        }).finally(() => {
            setJoiningRequestsLoading(false)
        })
    }

    const loadMoreJoiningRequests = () => {
        if (joiningRequests.length < joiningRequestTotalNumber) {
            loadJoiningRequests()
        }
    }

    const requestProcess = (joiningId, accept) => {
        setRequestProcessing(true)
        axios.put(
            "/api/group/confirm",
            {
                joiningId: joiningId,
                accept: accept
            },
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Request Process Successfully"
                })
                clearJoiningRequests()
                loadJoiningRequests()
            } else {
                notificationApi.error({
                    message: "Request Process Error",
                    description: "Wrong Parameters"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Request Process Error. Try again later"
            })
        }).finally(() => {
            setRequestProcessing(false)
        })
    }

    return (
        <>
            <Space direction="vertical" style={{width: "100%"}}>
                <Row>
                    <Col span={12}>
                        <Button
                            icon={<BiRefresh/>}
                            onClick={() => {
                                clearJoiningRequests()
                                loadJoiningRequests()
                            }}
                        />
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        Waiting Processing: {joiningRequestTotalNumber}
                    </Col>
                </Row>
                <div
                    style={{
                        height: "75vh", overflow: "auto",
                        border: "1px solid black", borderRadius: "5px"
                    }}
                >
                    <List
                        loading={joiningRequestsLoading}
                        locale={{emptyText: <span></span>}}
                        dataSource={joiningRequests}
                        renderItem={item => (
                            <List.Item
                                key={item.id}
                                actions={[
                                    <Tooltip key={item.id + "_tooltip"} title={item.comment}>
                                        <Button type="text">Message</Button>
                                    </Tooltip>,
                                    <Button
                                        key={item.id + "_reject_btn"}
                                        onClick={() => requestProcess(item.id, false)}
                                        loading={requestProcessing}
                                    >
                                        Reject
                                    </Button>,
                                    <Button
                                        key={item.id + "_accept_btn"}
                                        type="primary"
                                        onClick={() => requestProcess(item.id, true)}
                                        loading={requestProcessing}
                                    >
                                        Accept
                                    </Button>
                                ]}
                            >
                                <List.Item.Meta title={item.username} description={item.joinTime}/>
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
                                    joiningRequests.length < joiningRequestTotalNumber ?
                                        <Button
                                            loading={joiningRequestsLoading}
                                            onClick={() => loadMoreJoiningRequests()}
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
    )
}