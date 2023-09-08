import {useState} from "react";
import {Button, Col, List, Row, Space, Tooltip} from "antd";
import {BiRefresh} from "react-icons/bi";
import axios from "axios";

export default function ContactRequestTab({notification}) {

    const [contactRequestLoading, setContactRequestLoading] = useState(false)
    const [contactRequestTotalNumber, setContactRequestTotalNumber] = useState(0)
    const [contactRequests, setContactRequests] = useState([])
    const [processingContactRequest, setProcessingContactRequest] = useState(false)

    const clearContactRequests = () => {
        setContactRequestTotalNumber(0)
        contactRequests.splice(0, contactRequests.length)
        setContactRequests([...contactRequests])
    }

    const loadContactRequests = () => {
        setContactRequestLoading(true)
        axios.get(
            "/api/contact/by-type",
            {
                params: {
                    queryType: "CONTACT_REQUEST",
                    size: 20,
                    havingSize: contactRequests.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setContactRequestTotalNumber(total)
                if (contactRequests.length === 0) {
                    setContactRequests([...items])
                } else {
                    const arr = [...items]
                    arr.forEach(e => contactRequests.push(e))
                    setContactRequests([...contactRequests])
                }
            }        }).catch(err => {
            console.log(err)
            notification.error({
                message: "Load Contact Requests Error. Try again later"
            })
        }).finally(() => {
            setContactRequestLoading(false)
        })
    }

    const loadMoreContactRequests = () => {
        if (contactRequests.length < contactRequestTotalNumber) {
            loadContactRequests()
        }
    }

    const confirm = (contactRequest, accept) => {
        setProcessingContactRequest(true)
        axios.put(
            "/api/contact/confirm",
            {
                contactId: contactRequest.id,
                accept: accept
            },
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notification.success({
                    message: "Process Contact Request Successfully."
                })
                clearContactRequests()
                loadContactRequests()
            } else {
                notification.error({
                    message: "Process Contact Request Error.",
                    description: "Wrong Parameters"
                })
            }
        }).catch(err => {
            console.log(err)
            notification.error({
                message: "Process Contact Request Error. Try again later"
            })
        }).finally(() => {
            setProcessingContactRequest(false)
        })
    }

    const accept = (contactRequest) => {
        confirm(contactRequest, true)
    }

    const reject = (contactRequest) => {
        confirm(contactRequest, false)
    }

    return (
        <>
            <Space direction="vertical" style={{width: "100%"}}>
                <Row>
                    <Col span={12}>
                        <Button
                            icon={<BiRefresh/>}
                            onClick={() => {
                                clearContactRequests()
                                loadContactRequests()
                            }}
                        />
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        The Number of Contact Request: {contactRequestTotalNumber}
                    </Col>
                </Row>
                <div
                    style={{
                        height: "70vh", overflow: "auto",
                        border: "1px black solid", borderRadius: "5px"
                    }}
                >
                    <List
                        loading={contactRequestLoading}
                        locale={{emptyText: <></>}}
                        dataSource={contactRequests}
                        renderItem={contactRequest => (
                            <List.Item
                                actions={[
                                    <Tooltip
                                        key={contactRequest.id + "_comment"}
                                        title={contactRequest.comment}
                                    >
                                        <Button>Comment</Button>
                                    </Tooltip>,
                                    <Button
                                        type="primary" danger
                                        key={contactRequest.id + "_reject_btn"}
                                        loading={processingContactRequest}
                                        onClick={() => reject(contactRequest)}
                                    >
                                        Reject
                                    </Button>,
                                    <Button
                                        type="primary"
                                        key={contactRequest.id + "_accept_btn"}
                                        loading={processingContactRequest}
                                        onClick={() => accept(contactRequest)}
                                    >
                                        Accept
                                    </Button>,
                                ]}
                            >
                                <List.Item.Meta
                                    title={contactRequest.sourceUsername}
                                    description={contactRequest.createdTime}
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
                                    contactRequests.length < contactRequestTotalNumber ?
                                        <Button
                                            loading={contactRequestLoading}
                                            onClick={() => loadMoreContactRequests()}
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