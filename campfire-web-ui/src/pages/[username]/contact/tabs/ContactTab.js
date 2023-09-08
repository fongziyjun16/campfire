import {Button, Col, Divider, Form, Input, List, Popconfirm, Row, Space} from "antd";
import {useEffect, useState} from "react";
import {BiRefresh} from "react-icons/bi";
import axios from "axios";
import {useSelector} from "react-redux";
import {selectUsername} from "@/lib/redux/slices/usernameSlice";

const {TextArea} = Input

export default function ContactTab({notification}) {

    const username = useSelector(selectUsername)
    const [contactsLoading, setContactLoading] = useState(false)
    const [contactTotalNumber, setContactTotalNumber] = useState(0)
    const [contacts, setContacts] = useState([])
    const [removingContact, setRemovingContact] = useState(false)

    useEffect(() => {
        loadContacts()
    }, []);

    const clearContacts = () => {
        setContactTotalNumber(0)
        contacts.splice(0, contacts.length)
        setContacts([...contacts])
    }

    const loadContacts = () => {
        setContactLoading(true)
        axios.get(
            "/api/contact/by-type",
            {
                params: {
                    queryType: "ACCEPT",
                    size: 20,
                    havingSize: contacts.length
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {total, items} = resp.data.data
                setContactTotalNumber(total)
                if (contacts.length === 0) {
                    setContacts([...items])
                } else {
                    const arr = [...items]
                    arr.forEach(e => contacts.push(e))
                    setContacts([...contacts])
                }
            }
        }).catch(err => {
            console.log(err)
            notification.error({
                message: "Load Contacts Error. Try again later"
            })
        }).finally(() => {
            setContactLoading(false)
        })
    }

    const loadMoreContacts = () => {
        if (contacts.length < contactTotalNumber) {
            loadContacts()
        }
    }

    const breakContact = (contact) => {
        setRemovingContact(true)
        axios.delete(
            "/api/contact/break",
            {
                params: {
                    contactId: contact.id
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notification.success({
                    message: "Break Contact Successfully.",
                })
                clearContacts()
                loadContacts()
            } else {
                notification.error({
                    message: "Break Contact Error. Try again later",
                    description: "Wrong Parameters"
                })
            }
        }).catch(err => {
            console.log(err)
            notification.error({
                message: "Break Contact Error. Try again later"
            })
        }).finally(() => {
            setRemovingContact(false)
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
                                clearContacts()
                                loadContacts()
                            }}
                        />
                    </Col>
                    <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                        The Number of Contact: {contactTotalNumber}
                    </Col>
                </Row>
                <div
                    style={{
                        height: "70vh", overflow: "auto",
                        border: "1px black solid", borderRadius: "5px"
                    }}
                >
                    <List
                        loading={contactsLoading}
                        locale={{emptyText: <></>}}
                        dataSource={contacts}
                        renderItem={contact => (
                            <List.Item
                                actions={[
                                    <Popconfirm
                                        key={contact.id + "_remove_btn"}
                                        title="Remove the contact"
                                        description="Are you sure to remove this contact?"
                                        onConfirm={() => breakContact(contact)}
                                        okText="Yes" cancelText="No"
                                    >
                                        <Button type="primary" danger loading={removingContact}>Break</Button>
                                    </Popconfirm>
                                ]}
                            >
                                <List.Item.Meta
                                    title={contact.sourceUsername === username ? contact.targetUsername : contact.sourceUsername}
                                    description={contact.createdTime}
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
                                    contacts.length < contactTotalNumber ?
                                        <Button
                                            loading={contactsLoading}
                                            onClick={() => loadMoreContacts()}
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