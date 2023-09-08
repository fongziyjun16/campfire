import {Button, Col, List, Popconfirm, Row, Space, Tooltip} from "antd";
import {BiRefresh} from "react-icons/bi";
import {useEffect, useState} from "react";
import axios from "axios";

export default function WaitingListTab({notification}) {

    const [waitingContactsLoading, setWaitingContactsLoading] = useState(false)
    const [waitingContactTotalNumber, setWaitingContactTotalNumber] = useState(0)
    const [waitingContacts, setWaitingContacts] = useState([])

    useEffect(() => {
        loadWaitingContacts()
    }, []);

    const clearWaitingContacts = () => {
        setWaitingContactTotalNumber(0)
        waitingContacts.splice(0, waitingContacts.length)
        setWaitingContacts([...waitingContacts])
    }

    const loadWaitingContacts = () => {
        setWaitingContactsLoading(true)
        axios.get(
            "/api/contact/by-type",
            {
                params: {
                    queryType: "WAITING",
                    size: 20,
                    havingSize: waitingContacts.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setWaitingContactsLoading(total)
                if (waitingContacts.length === 0) {
                    setWaitingContacts([...items])
                } else {
                    const arr = [...items]
                    arr.forEach(e => waitingContacts.push(e))
                    setWaitingContacts([...waitingContacts])
                }
            }        }).catch(err => {
            console.log(err)
            notification.error({
                message: "Load Waiting Contacts Error. Try again later"
            })
        }).finally(() => {
            setWaitingContactsLoading(false)
        })
    }

    const loadMoreWaitingContacts = () => {
        if (waitingContacts.length < waitingContactTotalNumber) {
            loadWaitingContacts()
        }
    }

    return (
        <>
            <Space direction="vertical" style={{width: "100%"}}>
                <Row>
                    <Col span={12}>
                        <Button
                            icon={<BiRefresh/>}
                            onClick={() => {
                                clearWaitingContacts()
                                loadWaitingContacts()
                            }}
                        />
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        The Number of Waiting Contact: {waitingContactTotalNumber}
                    </Col>
                </Row>
                <div
                    style={{
                        height: "70vh", overflow: "auto",
                        border: "1px black solid", borderRadius: "5px"
                    }}
                >
                    <List
                        loading={waitingContactsLoading}
                        locale={{emptyText: <></>}}
                        dataSource={waitingContacts}
                        renderItem={waitingContact => (
                            <List.Item
                                actions={[
                                    <Tooltip
                                        key={waitingContact.id + "_comment"}
                                        title={waitingContact.comment}
                                    >
                                        <Button type="text">Leaving Message</Button>
                                    </Tooltip>,
                                ]}
                            >
                                <List.Item.Meta
                                    title={waitingContact.targetUsername}
                                    description={waitingContact.createdTime}
                                />
                            </List.Item>
                        )}
                        loadMore={
                            <div
                                style={{
                                    textAlign: 'center', marginTop: 12,
                                    height: 32, lineHeight: '32px',
                                }}
                            >
                                {
                                    waitingContacts.length < waitingContactTotalNumber ?
                                        <Button
                                            loading={waitingContactsLoading}
                                            onClick={() => loadMoreWaitingContacts()}
                                        >
                                            load more
                                        </Button> :
                                        <span>------ Not More Contacts ------</span>
                                }
                            </div>
                        }
                    />
                </div>
            </Space>
        </>
    )
}