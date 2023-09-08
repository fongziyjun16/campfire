import MainLayout from "@/pages/[username]/layout";
import {
    Button,
    Card,
    Col,
    Divider,
    Form,
    Input,
    Layout,
    List,
    notification,
    Popconfirm,
    Row,
    Space,
    Typography
} from "antd";
import InfiniteScroll from 'react-infinite-scroll-component';
import {HiDocumentPlus} from "react-icons/hi2";
import {useEffect, useState} from "react";
import dynamic from "next/dynamic";
import axios from "axios";

const NoteEditor = dynamic(
    () => import("./NoteEditor"),
    {
        ssr: false
    }
)

const {Sider, Content} = Layout

function NotePage() {

    const [notificationApi, notificationContextHolder] = notification.useNotification()
    const [selectedNoteId, setSelectedNoteId] = useState("")
    const [noteHeadListLoading, setNoteHeadListLoading] = useState(false)
    const [noteHeadTotalNumber, setNoteHeadTotalNumber] = useState(0)
    const [noteHeads, setNoteHeads] = useState([])
    const [saving, setSaving] = useState(false)
    const [updatedTime, setUpdatedTime] = useState("")
    const [title, setTitle] = useState("New Note")
    const [editorKey, setEditorKey] = useState("1")
    const [content, setContent] = useState(null)

    useEffect(() => {
        loadNoteHeads()
    }, []);

    const loadNoteHeads = () => {
        setNoteHeadListLoading(true)
        axios.get(
            "/api/note/heads",
            {
                params: {
                    size: 30,
                    havingSize: noteHeads.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, noteHeads: newNoteHeads} = resp.data.data
                // console.log(newNoteHeads)
                setNoteHeadTotalNumber(total)
                if (noteHeads.length === 0) {
                    setNoteHeads([...newNoteHeads])
                } else {
                    const arr = [...newNoteHeads]
                    arr.forEach(element => noteHeads.push(element))
                    setNoteHeads([...noteHeads])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Notes Error. Try again later"
            })
        }).finally(() => {
            setNoteHeadListLoading(false)
        })
    }

    const loadMoreNoteHeads = () => {
        if (noteHeads.length < noteHeadTotalNumber) {
            // console.log("load more")
            loadNoteHeads()
        }
    }

    const updateEditorKey = () => {
        setEditorKey(String(Number(editorKey) + 1))
    }

    const readNote = (noteId, title) => {
        axios.get(
            "/api/note/" + noteId
        ).then(resp => {
            if (resp.data?.data) {
                // console.log(JSON.parse(resp.data.data))
                const currContent = JSON.parse(resp.data.data)
                setSelectedNoteId(noteId)
                setTitle(title)
                setContent(currContent)
                updateEditorKey()
            } else {
                notificationApi.error({
                    message: "Cannot Read Note"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Fail to Read Note. Try again later"
            })
        })
    }

    const saveNote = () => {
        if (title.trim().length <= 0) {
            notificationApi.warning({
                message: "Note Title CANNOT BE EMPTY"
            })
            return
        }
        if (content) {
            setSaving(true)
            let resp = selectedNoteId === "" ?
                axios.post(
                    "/api/note",
                    {
                        title: title,
                        content: JSON.stringify(content)
                    },
                    {
                        headers: {
                            "Content-Type": "application/json"
                        }
                    }
                ) :
                axios.put("/api/note/" + selectedNoteId,
                    {
                        title: title,
                        content: JSON.stringify(content)
                    },
                    {
                        headers: {
                            "Content-Type": "application/json"
                        }
                    }
                )
            resp.then(resp => {
                if (resp.data?.code === 200) {
                    notificationApi.success({
                        message: "Save Note Successfully"
                    })
                    setUpdatedTime(new Date().toLocaleDateString(
                        'en-us',
                        {
                            year: 'numeric', month: 'long', day: 'numeric',
                            hour: '2-digit', minute: '2-digit', second: '2-digit'
                        }
                    ))
                    console.log(resp.data.data)
                    setSelectedNoteId(resp.data.data)
                } else {
                    notificationApi.error({
                        message: "Save Note Error. Try again later"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Save Note Error. Try again later"
                })
            }).finally(() => {
                setSaving(false)
            });
        } else {
            notificationApi.error({
                message: "Note Content cannot be Error or Get Note Content Error"
            })
        }
    }

    return (
        <>
            {notificationContextHolder}
            <Layout style={{height: "100%"}}>
                <Sider
                    width={400}
                    style={{
                        backgroundColor: "white"
                    }}
                >
                    <Card
                        title="Your Notes"
                        extra={
                            <Button
                                icon={<HiDocumentPlus/>}
                                onClick={() => {
                                    setSelectedNoteId("")
                                    setTitle("New Note")
                                    setContent(null)
                                    setUpdatedTime("")
                                    updateEditorKey()
                                }}
                            >
                                New
                            </Button>
                        }
                        style={{height: "100%"}}
                    >
                        <div
                            id="scrollableDiv"
                            style={{
                                height: "75vh",
                                overflow: "auto"
                            }}
                        >
                            <InfiniteScroll
                                next={loadMoreNoteHeads}
                                hasMore={noteHeads.length < noteHeadTotalNumber}
                                loader={null}
                                dataLength={noteHeads.length}
                                endMessage={<Divider plain>No More Notes</Divider>}
                                scrollableTarget="scrollableDiv"
                            >
                                <List
                                    loading={noteHeadListLoading}
                                    locale={{emptyText: <span></span>}}
                                    dataSource={noteHeads}
                                    renderItem={noteHead => (
                                        <List.Item
                                            actions={[
                                                <Button
                                                    key={noteHead.id + "_read_btn"}
                                                    type="primary"
                                                    onClick={() => readNote(noteHead.id, noteHead.title)}
                                                >
                                                    Read
                                                </Button>
                                            ]}
                                        >
                                            <List.Item.Meta
                                                title={noteHead.title}
                                                description={noteHead.updatedTime}
                                            />
                                        </List.Item>
                                    )}
                                />
                            </InfiniteScroll>
                        </div>
                    </Card>
                </Sider>
                <Content
                    style={{
                        backgroundColor: "white",
                        overflow: "auto"
                    }}
                >
                    <Row>
                        <Col span={2}></Col>
                        <Col span={20}>
                            <Row style={{paddingTop: "10px"}}>
                                <Col span={12}>
                                    <Typography.Title
                                        level={2}
                                        editable={{
                                            onChange: setTitle
                                        }}
                                    >
                                        {title}
                                    </Typography.Title>
                                </Col>
                                <Col
                                    span={12}
                                    style={{
                                        display: "flex",
                                        flexDirection: "row-reverse",
                                        alignSelf: "center"
                                    }}
                                >
                                    <Button
                                        type="primary"
                                        onClick={() => saveNote()}
                                        loading={saving}
                                        style={{
                                            width: "160px"
                                        }}
                                    >
                                        Save
                                    </Button>
                                </Col>
                            </Row>
                            <Divider style={{margin: "0 0 0 0"}}/>
                            <Row style={{paddingTop: "5px"}}>
                                <Col span={24}>
                                    <Space
                                        direction="vertical"
                                        style={{
                                            width: "100%"
                                        }}
                                    >
                                        <div style={{textAlign: "right"}}>
                                            {
                                                updatedTime !== "" ?
                                                    "updated at " + updatedTime :
                                                    ""
                                            }
                                        </div>
                                        <div
                                            style={{
                                                height: "80vh", overflow: "auto",
                                                borderColor: "gray", borderStyle: "solid",
                                                borderRadius: "12px", borderWidth: "thin",
                                            }}
                                        >
                                            <NoteEditor
                                                key={editorKey}
                                                content={content}
                                                updateContent={setContent}
                                            />
                                        </div>
                                    </Space>
                                </Col>
                            </Row>
                        </Col>
                        <Col span={2}></Col>
                    </Row>
                </Content>
            </Layout>
        </>
    )
}

NotePage.pageLayout = MainLayout

export default NotePage