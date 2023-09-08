import {Button, Card, Carousel, Col, Form, Input, Layout, notification, Row, Space} from "antd";
import {useRouter} from "next/router";
import {AiOutlineBarcode, AiOutlineLock, AiOutlineUser} from "react-icons/ai";
import {useRef, useState} from "react";
import axios from "axios";
import {useDispatch} from "react-redux";
import {updateAccountId} from "@/lib/redux/slices/accountIdSlice";
import {updateUsername} from "@/lib/redux/slices/usernameSlice";
import {updateAccountStatus} from "@/lib/redux/slices/accountStatusSlice";
import {updateRoleNames} from "@/lib/redux/slices/roleNamesSlice";

const {Content} = Layout

export default function SignInIndex() {

    const router = useRouter()
    const [notificationApi, contextHolder] = notification.useNotification();
    const [loading, setLoading] = useState(false)
    const dispatch = useDispatch()
    const [signInForm] = Form.useForm()
    const [verificationForm] = Form.useForm()
    const [passwordResetForm] = Form.useForm()
    const [username, setUsername] = useState("")
    const usernameRegExp = new RegExp(/^[A-Za-z][A-Za-z0-9_@\.]{4,62}[A-Za-z0-9]$/)
    const carouselRef = useRef(null)
    const [requestVerificationCodeLoading, setRequestVerificationCodeLoading] = useState(false)
    const [countdownSeconds, setCountdownSeconds] = useState(60)
    const [countdownSecondsDisplay, setCountdownSecondsDisplay] = useState(true)

    const signInSubmit = (values) => {
        setLoading(true)
        const {username, password} = values
        axios.post(
            "/api/account/sign-in",
            {
                username: username,
                password: password
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
                const {code, message, data: authenticationInfo} = resp.data
                if (code === 200) {
                    if (authenticationInfo.roleNames.includes("regular_user")) {
                        result = true;
                        dispatch(updateAccountId(authenticationInfo.id))
                        dispatch(updateUsername(authenticationInfo.username))
                        setUsername(authenticationInfo.username)
                        dispatch(updateAccountStatus(authenticationInfo.status))
                        dispatch(updateRoleNames(authenticationInfo.roleNames))
                        if (authenticationInfo.status === 'VERIFIED') {
                            notificationApi.success({
                                message: "Sign In Success, Redirect to User Page"
                            })
                            setTimeout(() => {
                                router.replace("/" + username);
                            }, 100)
                        } else {
                            carouselRef.current.next()
                        }
                    } else {
                        resultDescription = "No Authorization to Sign In"
                    }
                } else {
                    resultDescription = message;
                }
            }
            if (!result) {
                notificationApi.error({
                    message: "Fail to Sign In",
                    description: resultDescription
                })
            }
        }).catch(err => {
            notificationApi.error({
                message: "Try Sign In Later"
            })
        }).finally(() => {
            setLoading(false)
        })
    }

    const requestVerificationCode = () => {
        setRequestVerificationCodeLoading(true)
        setCountdownSecondsDisplay(false)
        let waitingSeconds = 60
        setCountdownSeconds(waitingSeconds)
        const countDown = setInterval(() => {
            if (waitingSeconds === 0) {
                clearInterval(countDown)
                setRequestVerificationCodeLoading(false)
                setCountdownSecondsDisplay(true)
            } else {
                waitingSeconds -= 1
                setCountdownSeconds(waitingSeconds)
            }
        }, 1000)
        axios.get(
            "/api/account/request-account-verification",
        ).then(resp => {
            if (resp.data !== null && resp.data.code !== 200) {
                notificationApi.error({
                    message: "Error Message",
                    description: resp.data.message
                })
            }
        }).catch(err => {
            notificationApi.error({
                message: "Try Request Later"
            })
        })
    }

    const accountVerify = (values) => {
        setLoading(true)
        const {verificationCode} = values
        axios.get(
            "/api/account/account-verification?code=" + verificationCode
        ).then(resp => {
            let result = false
            let resultDescription = ""
            if (resp.data !== null) {
                const {code, message} = resp.data
                if (code === 200) {
                    result = true
                    notificationApi.success({
                        message: "Verification Success, Redirect to User Page"
                    })
                    setTimeout(() => {
                        router.replace("/" + username)
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
                message: "Try Verify Later"
            })
        }).finally(() => {
            setLoading(false)
        })
    }

    const requestPasswordReset = () => {
        if (usernameRegExp.test(username)) {
            axios.get(
                "/api/account/request-password-reset/" + username
            ).then(resp => {
                let result = false
                let resultDescription = "Try to reset password later"
                if (resp.data !== null) {
                    const {code, message} = resp.data
                    if (code === 200) {
                        result = true
                        notificationApi.success({
                            message: "Request Password Reset Successfully, Check your email please"
                        })
                        carouselRef.current.goTo(2, false)
                    } else {
                        resultDescription = message
                    }
                }
                if (!result) {
                    notificationApi.error({
                        message: "Request Password Reset Failure",
                        description: resultDescription
                    })
                }
            }).catch(err => {
                notificationApi.error({
                    message: "Try to reset password later"
                })
            }).finally(()=> {

            })
        } else {
            notificationApi.error({
                message: "Please input valid username before reset password"
            })
        }
    }

    const resetPassword = (values) => {
        const {code, newPassword, retypeNewPassword} = values
        if (newPassword === retypeNewPassword) {
            setLoading(true)
            axios.post(
                "/api/account/password-reset",
                {
                    username: username,
                    code: code,
                    password: newPassword
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                let result = false
                let resultDescription = "Try to Request Later"
                if (resp.data !== null) {
                    const {code, message} = resp.data
                    if (code === 200) {
                        result = true
                        notificationApi.success({
                            message: "Reset Successfully",
                            description: "Back to Sign In"
                        })
                        setTimeout(() => {
                            carouselRef.current.goTo(0, false)
                        }, 1000)
                    } else {
                        resultDescription = message
                    }
                }
                if (!result) {
                    notificationApi.error({
                        message: "Reset Failure",
                        description: resultDescription
                    })
                }
            }).catch(err => {
                notificationApi.error({
                    message: "Try to Reset Later"
                })
            }).finally(() => {
                setLoading(false)
            })
        } else {
            notificationApi.error({
                message: "New Password NOT MATCH Retype New Password"
            })
        }
    }

    return (
        <>
            {contextHolder}
            <Layout style={{height: "100vh"}}>
                <Content
                    style={{
                        backgroundImage: "url('/images/sign-in/bg.jpeg')",
                        backgroundRepeat: "no-repeat",
                        backgroundSize: "cover",
                        backgroundAttachment: "fixed"
                    }}
                >
                    <Row align="middle" style={{height: "100%"}}>
                        <Col span={12}></Col>
                        <Col span={12} style={{display: "flex", alignItems: "center", justifyContent: "center"}}>
                            <Card style={{width: "400px", height: "400px", backgroundColor: "rgba(255, 255, 255, .6)"}}>
                                <Carousel dots={false} infinite={false} ref={carouselRef} effect="fade"
                                          beforeChange={(current, next) => {
                                              switch (next) {
                                                  case 0:
                                                      signInForm.resetFields()
                                                      break
                                                  case 1:
                                                      verificationForm.resetFields()
                                                      break
                                                  case 2:
                                                      passwordResetForm.resetFields()
                                                      break;
                                              }
                                          }}
                                >
                                    <div>
                                        <Space direction="vertical" align="center" style={{width: "100%"}}>
                                            <h1 style={{fontSize: "40px"}}>Good Day</h1>
                                            <Form
                                                name="signInForm"
                                                form={signInForm}
                                                autoComplete="off"
                                                style={{width: "300px"}}
                                                onFinish={signInSubmit}
                                            >
                                                <Form.Item
                                                    name="username"
                                                    rules={[
                                                        {pattern: usernameRegExp, message: "Length in 6 to 64, Start with [A-Z, a-z], End with [A-Z, a-z, 0-9], Allow Character[A-Z, a-z, 0-9, _, ., @]"},
                                                        {required: true, message: "Please input your username!"}
                                                    ]}
                                                >
                                                    <Input prefix={<AiOutlineUser/>} placeholder="Usernanme" onChange={(e) => setUsername(e.target.value)}/>
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
                                                <Form.Item>
                                                    <Button loading={loading} type="primary" htmlType="submit" style={{width: "100%"}}>
                                                        Sign In
                                                    </Button>
                                                    <Row>
                                                        <Col span={12}>
                                                            or
                                                            <Button type="link" onClick={() => router.replace("/sign-up")}>
                                                                sign up now
                                                            </Button>
                                                        </Col>
                                                        <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                                                            <Button type="link" onClick={requestPasswordReset}>
                                                                reset password
                                                            </Button>
                                                        </Col>
                                                    </Row>
                                                </Form.Item>
                                            </Form>
                                        </Space>
                                    </div>
                                    <div>
                                        <Space direction="vertical" align="center" style={{width: "100%"}}>
                                            <h1 style={{fontSize: "36px"}}>Account Verification</h1>
                                            <Form
                                                name="verificationForm"
                                                form={verificationForm}
                                                autoComplete="off"
                                                style={{width: "300px"}}
                                                onFinish={accountVerify}
                                            >
                                                <Form.Item
                                                    name="verificationCode"
                                                    rules={[
                                                        {required: true, message: "Please input verification code"}
                                                    ]}
                                                >
                                                    <Space.Compact style={{width: "100%"}}>
                                                        <Input  prefix={<AiOutlineBarcode/>} placeholder="Verification Code"/>
                                                        <Button
                                                            type="primary"
                                                            loading={requestVerificationCodeLoading}
                                                            onClick={requestVerificationCode}
                                                        >
                                                            Request
                                                        </Button>
                                                    </Space.Compact>
                                                </Form.Item>
                                                <span hidden={countdownSecondsDisplay}>{countdownSeconds}s later, request verification code again</span>
                                                <Form.Item>
                                                    <Button type="primary" htmlType="submit" loading={loading} style={{width: "100%"}}>
                                                        Verify
                                                    </Button>
                                                    or
                                                    <Button type="link" onClick={() => carouselRef.current.prev()}>
                                                        back to sign in
                                                    </Button>
                                                </Form.Item>
                                            </Form>
                                        </Space>
                                    </div>
                                    <div>
                                        <Space direction="vertical" align="center" style={{width: "100%"}}>
                                            <h1 style={{fontSize: "36px"}}>Reset Password</h1>
                                            <Form
                                                name="passwordResetForm"
                                                form={passwordResetForm}
                                                autoComplete="off"
                                                style={{width: "300px"}}
                                                onFinish={resetPassword}
                                            >
                                                <Form.Item
                                                    name="code"
                                                    rules={[
                                                        {required: true, message: "Please input password reset code"}
                                                    ]}
                                                >
                                                    <Input prefix={<AiOutlineBarcode/>} placeholder="Password Reset Code"/>
                                                </Form.Item>
                                                <Form.Item
                                                    name="newPassword"
                                                    rules={[
                                                        {min: 6, max: 64, message: "Password Length, 6 to 64"},
                                                        {required: true, message: "Please input new password"}
                                                    ]}
                                                >
                                                    <Input.Password  prefix={<AiOutlineLock/>} placeholder="New Password"/>
                                                </Form.Item>
                                                <Form.Item
                                                    name="retypeNewPassword"
                                                    rules={[
                                                        {required: true, message: "Please retype new password"}
                                                    ]}
                                                >
                                                    <Input.Password  prefix={<AiOutlineLock/>} placeholder="Retype New Password"/>
                                                </Form.Item>
                                                <Form.Item>
                                                    <Button type="primary" htmlType="submit" loading={loading} style={{width: "100%"}}>
                                                        Reset
                                                    </Button>
                                                    or
                                                    <Button type="link" onClick={() => carouselRef.current.goTo(0, false)}>
                                                        back to sign in
                                                    </Button>
                                                </Form.Item>
                                            </Form>
                                        </Space>
                                    </div>
                                </Carousel>
                            </Card>
                        </Col>
                    </Row>
                </Content>
            </Layout>
        </>
    )
}