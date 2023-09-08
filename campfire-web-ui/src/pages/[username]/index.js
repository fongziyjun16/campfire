import {useEffect, useState} from "react";
import {useRouter} from "next/router";
import {Button, Card, Col, Divider, Image, Layout, notification, Row, Space} from "antd";
import {useSelector} from "react-redux";
import {selectAvatarUrl} from "@/lib/redux/slices/avatarUrlSlice";
import {selectUsername} from "@/lib/redux/slices/usernameSlice";
import axios from "axios";

const {Sider, Content} = Layout

export default function MainPage() {

    const username = useSelector(selectUsername)
    const avatarUrl = useSelector(selectAvatarUrl)
    const [description, setDescription] = useState("")
    const [notificationApi, contextHolder] = notification.useNotification();
    const router = useRouter()

    useEffect(() => {
        loadDescription()
    }, []);

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

    return (
        <>
            {contextHolder}
            <Row>
                <Col span={4}></Col>
                <Col span={16}>
                    <Layout style={{height: "100vh"}}>
                        <Sider width={250} style={{backgroundColor: "#CDE2FF"}}>
                        </Sider>
                        <Content style={{backgroundColor: "white", paddingLeft: "5px"}}>
                            <Space direction="vertical" style={{width: "100%"}}>
                                <Space direction="vertical" style={{width: "100%", paddingTop: "50px"}}>
                                    <Entry name={"Username"} value={username}/>
                                    <Entry name={"Description"} value={description}/>
                                </Space>
                                <Card title="Dive In">
                                    <Space size={[8, 16]} wrap>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/group")}>
                                            Group
                                        </Button>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/task")}>
                                            Task
                                        </Button>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/note")}>
                                            Note
                                        </Button>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/message")}>
                                            Message
                                        </Button>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/contact")}>
                                            My Contact
                                        </Button>
                                        <Button style={{width: "200px"}}
                                                onClick={() => router.push("/" + username + "/settting")}>
                                            Setting
                                        </Button>
                                    </Space>
                                </Card>
                                <Button type="text"
                                    onClick={() => router.push("/" + username + "/setting")}>
                                    Setting
                                </Button>
                            </Space>
                        </Content>
                    </Layout>
                </Col>
                <Col span={4}></Col>
            </Row>
        </>
    )
}

function Entry({name, value}) {
    return (
        <>
            <div>
                <span style={{fontWeight: "bold"}}>{name}: </span>
                <span>{value}</span>
            </div>
        </>
    )
}