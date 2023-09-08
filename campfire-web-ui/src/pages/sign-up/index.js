import {Button, Card, Col, Form, Input, Layout, notification, Row, Space} from "antd";
import {useRouter} from "next/router";
import {AiOutlineLock, AiOutlineMail, AiOutlineUser} from "react-icons/ai";
import {useState} from "react";
import axios from "axios";

const {Content} = Layout

export default function SignUpIndex() {

    const router = useRouter()
    const [notificationApi, contextHolder] = notification.useNotification();
    const [loading, setLoading] = useState(false)

    const signUpSubmit = (values) => {
        const {username, password, retypePassword, email} = values
        if (password === retypePassword) {
            setLoading(true)
            axios.post(
                "/api/account/sign-up",
                {
                    username: username,
                    password: password,
                    email: email
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                let result = false
                let resultDescription = ""
                if (resp.data !== null) {
                    const {code, message} = resp.data
                    if (code === 200) {
                        result = true
                        notificationApi.success({
                            message: "Sign Up Success, Redirect to Sign In"
                        })
                        setTimeout(() => {
                            router.replace("/sign-in")
                        }, 2000)
                    } else {
                        resultDescription = message
                    }
                }
                if (!result) {
                    notificationApi.error({
                        message: "Verification Failure",
                        description: resultDescription
                    })
                }
            }).catch(err => {
                notificationApi.error({
                    message: "Try Sign Up Later"
                })
            }).finally(() => {
                setLoading(false)
            });
        } else {
            notificationApi.error({
                message: "Password does not math Retype Password"
            })
        }
    }

    return (
        <>
            {contextHolder}
            <Layout style={{height: "100vh"}}>
                <Content
                    style={{
                        backgroundImage: "url('/images/sign-up/bg.jpg')",
                        backgroundRepeat: "no-repeat",
                        backgroundSize: "cover",
                        backgroundAttachment: "fixed"
                    }}
                >
                    <Row align="middle" style={{height: "100%"}}>
                        <Col span={12} style={{display: "flex", alignItems: "center", justifyContent: "center"}}>
                            <Card style={{width: "400px", height: "500px", backgroundColor: "rgba(255, 255, 255, .6)"}}>
                                <Space direction="vertical" align="center" style={{width: "100%"}}>
                                    <h1 style={{fontSize: "40px"}}>Welcome</h1>
                                    <Form
                                        autoComplete="off"
                                        style={{width: "300px"}}
                                        onFinish={signUpSubmit}
                                    >
                                        <Form.Item
                                            name="username"
                                            rules={[
                                                {pattern: new RegExp(/^[A-Za-z][A-Za-z0-9_@\.]{4,62}[A-Za-z0-9]$/), message: "Length in 6 to 64, Start with [A-Z, a-z], End with [A-Z, a-z, 0-9], Allow Character[A-Z, a-z, _, ., @]"},
                                                {required: true, message: "Please input your username!"}
                                            ]}
                                        >
                                            <Input prefix={<AiOutlineUser/>} placeholder="Usernanme"/>
                                        </Form.Item>
                                        <Form.Item
                                            name="password"
                                            rules={[
                                                {min: 6, max: 64, message: "Length in 6 to 64"},
                                                {required: true, message: "Please input your password!"}
                                            ]}
                                        >
                                            <Input.Password prefix={<AiOutlineLock/>} placeholder="Password"/>
                                        </Form.Item>
                                        <Form.Item
                                            name="retypePassword"
                                            rules={[{required: true, message: "Please retype your password!"}]}
                                        >
                                            <Input.Password prefix={<AiOutlineLock/>} placeholder="Retype Password"/>
                                        </Form.Item>
                                        <Form.Item
                                            name="email"
                                            rules={[
                                                {required: true, message: "Please input your email!"},
                                                {type: "email", message: "Please input email in valid format!"}
                                            ]}
                                        >
                                            <Input prefix={<AiOutlineMail/>} placeholder="Email"/>
                                        </Form.Item>
                                        <Form.Item>
                                            <Button loading={loading} type="primary" htmlType="submit" style={{width: "100%"}}>
                                                Sign Up
                                            </Button>
                                            or
                                            <Button type="link" onClick={() => router.replace("/sign-in")}>
                                                sign in now
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </Space>
                            </Card>
                        </Col>
                        <Col span={12}></Col>
                    </Row>
                </Content>
            </Layout>
        </>
    )
}