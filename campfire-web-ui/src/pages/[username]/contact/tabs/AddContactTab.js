import {useRef, useState} from "react";
import {Button, Card, Form, Input, Space} from "antd";
import axios from "axios";

const {TextArea} = Input

export default function AddContactTab({notification}) {

    const addContactFormRef = useRef(null)
    const [adding, setAdding] = useState(false)

    const addContact = (values) => {
        const {username, comment} = values
        if (username?.trim().length > 0 && comment?.trim().length > 0) {
            setAdding(true)
            axios.get(
                "/api/account/username/" + username
            ).then(resp => {
                if (resp.data?.code === 200) {
                    const {data} = resp.data
                    axios.post(
                        "/api/contact/build",
                        {
                            targetId: data.id,
                            comment: comment
                        },
                        {
                            headers: {
                                "Content-Type": "application/x-www-form-urlencoded"
                            }
                        }
                    ).then(resp => {
                        if (resp.data?.code === 200) {
                            notification.success({
                                message: "Add Contact Successfully."
                            })
                            addContactFormRef.current?.resetFields()
                        } else {
                            notification.error({
                                message: "Add Contact Error.",
                                description: "Wrong parameter"
                            })
                        }
                    }).catch(err => {
                        console.log(err)
                        notification.error({
                            message: "Add Contact Error. Try again later"
                        })
                    }).finally(() => {
                        setAdding(false)
                    })
                } else {
                    notification.error({
                        message: "Search User Error.",
                        description: "This user not found"
                    })
                    setAdding(false)
                }
            }).catch(err => {
                console.log(err)
                notification.error({
                    message: "Search User Error. Try again later"
                })
                setAdding(false)
            })
        } else {
            notification.warning({
                message: "Username & Comment cannot be empty"
            })
        }
    }

    return (
        <>
            <Space direction="vertical" style={{width: "100%"}}>
                <Card>
                    <Form
                        ref={addContactFormRef}
                        autoComplete="off"
                        onFinish={addContact}
                    >
                        <Form.Item
                            name="username"
                            label="Username"
                        >
                            <Input/>
                        </Form.Item>
                        <Form.Item
                            name="comment"
                            label="Comment"
                        >
                            <TextArea/>
                        </Form.Item>
                        <Form.Item style={{display: "flex", justifyContent: "flex-end"}}>
                            <Button type="primary" htmlType="submit" loading={adding}>
                                Add
                            </Button>
                        </Form.Item>
                    </Form>
                </Card>

            </Space>
        </>
    )
}