import {useRouter} from "next/router";
import {Button, Col, Divider, Image, Layout, Row, Space} from "antd";

const {Header, Footer, Content} = Layout;

const btnStyle = {
    width: "100px"
}

const subHeader = {
    fontSize: "40px"
}

const subParagraph = {
    fontSize: "25px"
}

const subImageColumnLeft = {
    display: "flex",
    justifyContent: "flex-start"
}

const subImageColumnRight = {
    display: "flex",
    justifyContent: "flex-end"
}

export default function Index() {

    const router = useRouter()

    return (
        <>
            <Layout style={{height: "100vh"}}>
                <Header style={{backgroundColor: "white"}}>
                    <Row>
                        <Col span={12}>
                            <span style={{fontSize: "30px", fontWeight: "bold"}}>Campfire</span>
                        </Col>
                        <Col span={12} style={{display: "flex", justifyContent: "flex-end"}}>
                            <Space>
                                <Button size="large" type="primary" style={btnStyle} onClick={() => router.replace("/sign-in")}>
                                    Sign In
                                </Button>
                                <Button size="large" style={btnStyle} onClick={() => router.replace("/sign-up")}>
                                    Sign Up
                                </Button>
                            </Space>
                        </Col>
                    </Row>
                </Header>
                <Divider style={{margin: "0px"}}/>
                <Content style={{backgroundColor: "white", overflow: "auto"}}>
                    <Row>
                        <Col span={4}></Col>
                        <Col span={16}>
                            <Space direction="vertical" size="large">
                                <div id="intro" style={{height: "300px", textAlign: "center"}}>
                                    <Space align="center" direction="vertical" size="small">
                                        <h1 style={{fontSize: "60px"}}>
                                            A Place to Study in Groups
                                        </h1>
                                        <p style={{textAlign: "center", fontSize: "30px"}}>
                                            Joining Groups, Accomplishing Tasks, Publishing Notes
                                        </p>
                                    </Space>
                                </div>
                                <div id="group" style={{height: "300px"}}>
                                    <Row>
                                        <Col span={12}>
                                            <h1 style={subHeader}>
                                                Groups
                                            </h1>
                                            <p style={subParagraph}>
                                                Creating or Joining Groups with specific Topics
                                            </p>
                                        </Col>
                                        <Col span={12} style={subImageColumnRight}>
                                            <Image src="/images/group.png" preview={false} width={300} height={300}/>
                                        </Col>
                                    </Row>
                                </div>
                                <div id="task" style={{height: "300px"}}>
                                    <Row>
                                        <Col span={12} style={subImageColumnLeft}>
                                            <Image src="/images/task.png" preview={false} width={300} height={300}/>
                                        </Col>
                                        <Col span={12}>
                                            <h1 style={subHeader}>
                                                Task
                                            </h1>
                                            <p style={subParagraph}>
                                                Creating and Publishing Daily Tasks in a Group to Keep Moving
                                            </p>
                                        </Col>
                                    </Row>
                                </div>
                                <div id="note" style={{height: "300px"}}>
                                    <Row>
                                        <Col span={12}>
                                            <h1 style={subHeader}>
                                                Note
                                            </h1>
                                            <p style={subParagraph}>
                                                Publishing Notes in a Group to Communicate
                                            </p>
                                        </Col>
                                        <Col span={12} style={subImageColumnRight}>
                                            <Image src="/images/note.png" preview={false} width={300} height={300}/>
                                        </Col>
                                    </Row>
                                </div>
                            </Space>
                        </Col>
                        <Col span={4}></Col>
                    </Row>
                    <Footer>
                        Copyright &copy; 2023 Campfire. All Rights Reserved
                    </Footer>
                </Content>
            </Layout>
        </>
    )
}