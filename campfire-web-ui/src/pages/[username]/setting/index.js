import MainLayout from "@/pages/[username]/layout";
import {Button, Card, Col, Divider, Form, Image, Input, notification, Row, Space, Upload} from "antd";
import {useEffect, useRef, useState} from "react";
import axios from "axios";
import {useSelector} from "react-redux";
import {selectAvatarUrl} from "@/lib/redux/slices/avatarUrlSlice";
import {AiOutlineUpload} from "react-icons/ai";

const {TextArea} = Input

function SettingPage() {

    const avatarUrl = useSelector(selectAvatarUrl)
    const [notificationApi, notificationContextHolder] = notification.useNotification();
    const [uploadingAvatar, setUploadingAvatar] = useState(false)
    const [description, setDescription] = useState("")
    const updateDescriptionFormRef = useRef(null)
    const [updatingDescription, setUpdatingDescription] = useState(false)
    const updatePasswordFormRef = useRef(null)
    const [updatingPassword, setUpdatingPassword] = useState(false)

    useEffect(() => {
        loadDescription()
    }, []);

    const beforeUploadAvatar = (file) => {
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/jpg' || file.type === 'image/png';
        if (!isJpgOrPng) {
            notificationApi.error({message: 'You can only upload JPG/JPEG/PNG file!'});
        }
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) {
            notificationApi.error({message: 'Image must smaller than 2MB!'});
        }
        return isJpgOrPng && isLt2M;
    }

    const uploadAvatar = (options) => {
        // console.log(options)
        setUploadingAvatar(true)
        axios.post(
            "/api/upload/avatar",
            {
                file: options.file
            },
            {
                headers: {
                    "Content-Type": "multipart/form-data"
                },
            },
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Upload Avatar Successfully"
                })
                location.reload()
            } else {
                notificationApi.error({
                    message: "Upload Avatar Error",
                    description: "File Not Empty and < 2MB, Only Accept PNG or JPEG"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Upload avatar error. Try again later"
            })
        }).finally(() => {
            setUploadingAvatar(false)
        })
    }

    const loadDescription = () => {
        axios.get(
            "/api/account/description"
        ).then(resp => {
            setDescription(resp.data.data)
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Get Description Error. Try again later"
            })
        })
    }

    const updateDescription = (values) => {
        const {description} = values
        if (description?.trim().length > 0) {
            setUpdatingDescription(true)
            axios.put(
                "/api/account/description",
                {
                    description: description
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    notificationApi.success({
                        message: "Update Description Successfully",
                    })
                    loadDescription()
                    updateDescriptionFormRef.current?.resetFields()
                } else {
                    notificationApi.error({
                        message: "Update Description Error",
                        description: "Wrong Parameters"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Update Description Error. Try again later"
                })
            }).finally(() => {
                setUpdatingDescription(false)
            })
        } else {
            notificationApi.warning({
                message: "Description cannot be empty"
            })
        }
    }

    const updatePassword = (values) => {
        const {oldPassword, newPassword, confirmPassword} = values
        if (newPassword !== confirmPassword) {
            notificationApi.warning({
                message: "New Password has to be equal to Old Password"
            })
            return
        }
        setUpdatingPassword(true);
        axios.put(
            "/api/account/password",
            {
                oldPassword: oldPassword,
                newPassword: newPassword
            },
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Update password successfully."
                })
                updatePasswordFormRef.current?.resetFields()
            } else {
                notificationApi.error({
                    message: "Update password error.",
                    description: "Wrong old password"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Update password error. Try again later"
            })
        }).finally(() => {
            setUpdatingPassword(false)
        })
    }

    return (
        <>
            {notificationContextHolder}
            <Row style={{paddingTop: "10px"}}>
                <Col span={4}></Col>
                <Col span={16}>
                    <Space direction="vertical" style={{width: "100%"}}>
                        <Card title="Upload Avatar">
                            <Space direction="vertical">
                                <Image
                                    width={200} src={avatarUrl}
                                    fallback="data:image/png;base64,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"
                                    style={{borderRadius: "5px"}}
                                />
                                <Upload maxCount={1} fileList={[]} beforeUpload={beforeUploadAvatar}
                                        customRequest={uploadAvatar}>
                                    <Button icon={<AiOutlineUpload/>} loading={uploadingAvatar}>Click to Upload
                                        Avatar</Button>
                                </Upload>
                            </Space>
                        </Card>
                        <Card title="Update Description">
                            <Space direction="vertical" style={{width: "100%"}}>
                                <span style={{fontStyle: "italic", fontSize: "20px"}}>{description}</span>
                                <Divider/>
                                <Form
                                    ref={updateDescriptionFormRef}
                                    autoComplete="off"
                                    onFinish={updateDescription}
                                    style={{width: "80%"}}
                                >
                                    <Form.Item name="description">
                                        <TextArea placeholder="New Description about Yourself"/>
                                    </Form.Item>
                                    <Form.Item style={{display: "flex", justifyContent: "flex-end"}}>
                                        <Button type="primary" htmlType="submit" loading={updatingDescription}>
                                            Update
                                        </Button>
                                    </Form.Item>
                                </Form>
                            </Space>
                        </Card>
                        <Card title="Update Password">
                            <Form
                                ref={updatePasswordFormRef}
                                autoComplete="off"
                                onFinish={updatePassword}
                                style={{width: "500px"}}
                                labelCol={{
                                    span: 8,
                                }}
                                wrapperCol={{
                                    span: 16,
                                }}
                            >
                                <Form.Item
                                    name="oldPassword"
                                    label="Old Password"
                                    rules={[
                                        {min: 6, max: 64, message: "Length in 6 to 64"},
                                        {required: true, message: "Please input your password!"}
                                    ]}
                                >
                                    <Input.Password allowClear/>
                                </Form.Item>
                                <Form.Item
                                    name="newPassword"
                                    label="New Password"
                                    rules={[
                                        {min: 6, max: 64, message: "Length in 6 to 64"},
                                        {required: true, message: "Please input your password!"}
                                    ]}
                                >
                                    <Input.Password allowClear/>
                                </Form.Item>
                                <Form.Item
                                    name="confirmPassword"
                                    label="Confirm Password"
                                    rules={[
                                        {min: 6, max: 64, message: "Length in 6 to 64"},
                                        {required: true, message: "Please input your password!"}
                                    ]}
                                >
                                    <Input.Password allowClear/>
                                </Form.Item>
                                <Form.Item wrapperCol={{offset: 8, span: 16,}}>
                                    <Button type="primary" htmlType="submit" loading={updatingPassword}>
                                        Update
                                    </Button>
                                </Form.Item>
                            </Form>
                        </Card>
                    </Space>
                </Col>
                <Col span={4}></Col>
            </Row>
        </>
    )
}

SettingPage.pageLayout = MainLayout

export default SettingPage