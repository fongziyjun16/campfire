import {useEffect, useRef, useState} from "react";
import {Button, Carousel, Col, Divider, Dropdown, Form, Input, Layout, List, Row, Space, Typography} from "antd";
import {AiOutlineArrowLeft} from "react-icons/ai";
import {useSelector} from "react-redux";
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";
import axios from "axios";
import {GrRefresh} from "react-icons/gr";

const {Header, Content} = Layout
const {TextArea} = Input
const {Title, Paragraph} = Typography

export default function PostTab({notificationApi, groupId, leaderId}) {

    const accountId = useSelector(selectAccountId)
    const carouselRef = useRef(null)
    const [postListLoading, setPostListLoading] = useState(false)
    const [postTotalNumber, setPostTotalNumber] = useState(0)
    const [posts, setPosts] = useState([])
    const [selectedPost, setSelectedPost] = useState(null)
    const postFormRef = useRef(null)
    const [publishing, setPublishing] = useState(false)
    const [commentListLoading, setCommentListLoading] = useState(false)
    const [commentTotalNumber, setCommentTotalNumber] = useState(0)
    const [comments, setComments] = useState([])
    const commentFormRef = useRef(null)
    const [commentPublishing, setCommentPublishing] = useState(false)


    useEffect(() => {
        setPostTotalNumber(0)
        posts.splice(0, posts.length)
        setPosts([...posts])
        loadPosts()
    }, [])

    const loadPosts = () => {
        setPostListLoading(true)
        axios.get(
            "/api/post/" + groupId,
            {
                params: {
                    size: 30,
                    havingSize: posts.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, postHeads: newPostHeads} = resp.data.data
                setPostTotalNumber(total)
                if (posts.length === 0) {
                    setPosts([...newPostHeads])
                } else {
                    const arr = [...newPostHeads]
                    arr.forEach(element => posts.push(element))
                    setPosts([...posts])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Query Group Posts Error. Try again later"
            })
        }).finally(() => {
            setPostListLoading(false)
        })
    }

    const loadMorePosts = () => {
        // console.log("load more")
        if (posts.length < postTotalNumber) {
            loadPosts();
        }
    }

    const renderPostActions = (post) => {
        const actions = []
        actions.push(
            <Button
                type="primary"
                onClick={() => {
                    setSelectedPost(post)
                    carouselRef.current.goTo(2)
                    setCommentTotalNumber(0)
                    comments.splice(0, comments.length)
                    setComments([...comments])
                    loadComments(post)
                }}
            >
                Read
            </Button>
        )
        if (leaderId === accountId) {
            actions.unshift(
                <Dropdown
                    menu={{
                        items: [
                            {
                                key: 'open',
                                label: (
                                    <Button onClick={() => changePostStatus(post.id, 'OPEN')}>
                                        Change To Open
                                    </Button>
                                )
                            },
                            {
                                key: 'close',
                                label: (
                                    <Button onClick={() => changePostStatus(post.id, 'CLOSE')}>
                                        Change To close
                                    </Button>
                                )
                            },
                            {
                                key: 'hide',
                                label: (
                                    <Button onClick={() => changePostStatus(post.id, 'HIDE')}>
                                        Change To Hide
                                    </Button>
                                )
                            },
                        ]
                    }}
                    arrow
                >
                    <Button>
                        Change Status
                    </Button>
                </Dropdown>
            )
        }
        return actions;
    }

    const changePostStatus = (postId, newStatus) => {
        const post = posts.find(post => post.id === postId)
        if (post && post.status !== newStatus) {
            setPostListLoading(true);
            axios.put(
                "/api/post/status",
                {
                    postId: postId,
                    status: newStatus
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    notificationApi.success({
                        message: "Update Post Successfully"
                    })
                    post.status = newStatus
                    setPosts([...posts])
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Update Post Status Error. Try again later"
                })
            }).finally(() => {
                setPostListLoading(false)
            })
        }
    }

    const publish = (values) => {
        const {title, content} = values
        if (title?.trim().length > 0 && content?.trim().length > 0) {
            setPublishing(true)
            axios.post(
                "/api/post",
                {
                    groupId: groupId,
                    title: title,
                    content: content
                },
                {
                    headers: {
                        "Content-Type": "application/json"
                    }
                }
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    notificationApi.success({
                        message: "Published Post"
                    })
                    carouselRef.current.goTo(0)
                    setPostTotalNumber(0)
                    posts.splice(0, posts.length)
                    setPosts([...posts])
                    loadPosts()
                } else {
                    notificationApi.error({
                        message: resp.data ? resp.data.message : "Publish Post Error. Try again later"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Publish Post Error. Try again later"
                })
            }).finally(() => {
                setPublishing(false)
            })
        } else {
            notificationApi.warning({
                message: "Publish Title and Content cannot be empty"
            })
        }
    }

    const loadComments = (post) => {
        setCommentListLoading(true);
        axios.get(
            "/api/comment/" + post.id,
            {
                params: {
                    size: 30,
                    havingSize: comments.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, commentHeads: newCommentHeads} = resp.data.data
                setCommentTotalNumber(total)
                if (comments.length === 0) {
                    setComments([...newCommentHeads])
                } else {
                    const arr = [...newCommentHeads]
                    arr.forEach(element => comments.push(element))
                    setComments([...comments])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Comments Error. Try again later"
            })
        }).finally(() => {
            setCommentListLoading(false)
        })
    }

    const loadMoreComments = () => {
        if (comments.length < commentTotalNumber) {
            loadComments(selectedPost)
        }
    }

    const publishComment = (values) => {
        const {comment} = values
        if (comment?.trim().length > 0) {
            setCommentPublishing(true)
            axios.post(
                "/api/comment",
                {
                    targetId: selectedPost.id,
                    targetType: "POST",
                    content: comment
                },
                {
                    headers: {
                        "Content-Type": "application/json"
                    }
                }
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    notificationApi.success({
                        message: "Publish Comment Successfully"
                    })
                    setPostTotalNumber(0)
                    posts.splice(0, posts.length)
                    setPosts([...posts])
                    loadComments()
                } else {
                    notificationApi.error({
                        message: "Cannot Publish Comment. Wrong Params"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Publish Comment Error. Try Again Later"
                })
            }).finally(() => {
                setCommentPublishing(false)
            })
        }
    }

    const renderCommentActions = (comment) => {
        const actions = []
        if (leaderId === accountId) {
            actions.push(
                <Dropdown
                    arrow
                    placement="bottom"
                    menu={{
                        items: [
                            {
                                key: "open",
                                label: (
                                    <Button onClick={() => changeCommentStatus(comment.id, "OPEN")}>
                                        Change To Open
                                    </Button>
                                )
                            },
                            {
                                key: "hide",
                                label: (
                                    <Button onClick={() => changeCommentStatus(comment.id, "HIDE")}>
                                        Change To Hide
                                    </Button>
                                )
                            },
                        ]
                    }}
                >
                    <Button>Change Comment Status</Button>
                </Dropdown>
            )
        }
        return actions
    }

    const changeCommentStatus = (commentId, newStatus) => {
        const comment = comments.find(comment => comment.id === commentId)
        if (comment?.status !== newStatus) {
            axios.put(
                "/api/comment/status",
                {
                    id: commentId,
                    status: newStatus
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    notificationApi.success({
                        message: "Change Comment Successfully"
                    })
                    const comment = comments.find(comment => comment.id === commentId)
                    comment.status = newStatus
                    setComments([...comments])
                } else {
                    notificationApi.error({
                        message: "Change Comment Status Error. Wrong Params"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Change Comment Status Error. Try again later"
                })
            })
        }
    }

    return (
        <>
            <Row>
                <Col span={2}></Col>
                <Col span={20}>
                    <Carousel
                        ref={carouselRef}
                        dots={null}
                        infinite={false}
                        effect="fade"
                    >
                        <div>
                            <Layout style={{height: "100%"}}>
                                <Header style={{backgroundColor: "white"}}>
                                    <Row>
                                        <Col span={12}>
                                            <Button
                                                onClick={() => {
                                                    setPostTotalNumber(0)
                                                    posts.splice(0, posts.length)
                                                    setPosts([...posts])
                                                    loadPosts()
                                                }}
                                            >
                                                <GrRefresh/>
                                            </Button>
                                        </Col>
                                        <Col span={12} style={{textAlign: "right"}}>
                                            <Button type="primary" onClick={() => carouselRef.current.goTo(1)}>
                                                Publish New Post
                                            </Button>
                                        </Col>
                                    </Row>
                                </Header>
                                <Divider style={{margin: "0 0 0 0"}}/>
                                <Content style={{backgroundColor: "white"}}>
                                    <List
                                        loading={postListLoading}
                                        locale={{emptyText: <span></span>}}
                                        dataSource={posts}
                                        renderItem={post => (
                                            <List.Item
                                                key={post.id}
                                                actions={renderPostActions(post)}
                                            >
                                                <List.Item.Meta
                                                    title={post.title + " - " + post.username}
                                                    description={post.status + " " + post.createdTime}
                                                />
                                            </List.Item>
                                        )}
                                        loadMore={
                                            <div
                                                style={{
                                                    textAlign: 'center',
                                                    marginTop: 12,
                                                    height: 32,
                                                    lineHeight: '32px',
                                                }}
                                            >
                                                {
                                                    posts.length < postTotalNumber ?
                                                        <Button onClick={() => loadMorePosts()}>
                                                            Load More
                                                        </Button> :
                                                        <Divider>No more Posts</Divider>
                                                }
                                            </div>
                                        }
                                        style={{
                                            height: "75vh",
                                            overflow: "auto"
                                        }}
                                    />
                                </Content>
                            </Layout>
                        </div>
                        <div>
                            <Layout style={{height: "100%"}}>
                                <Header style={{backgroundColor: "white"}}>
                                    <Row>
                                        <Col span={12}>
                                            <Button
                                                type="text"
                                                icon={<AiOutlineArrowLeft/>}
                                                onClick={() => {
                                                    postFormRef.current?.resetFields()
                                                    carouselRef.current.goTo(0)
                                                }}
                                            >
                                                Go Back
                                            </Button>
                                        </Col>
                                        <Col span={12}></Col>
                                    </Row>
                                </Header>
                                <Divider style={{margin: "0 0 0 0"}}/>
                                <Content style={{backgroundColor: "white", display: "flex", justifyContent: "center"}}>
                                    <Form
                                        name="postForm"
                                        ref={postFormRef}
                                        layout="vertical"
                                        autoComplete="off"
                                        style={{
                                            paddingTop: "10px",
                                            width: "90%"
                                        }}
                                        onFinish={publish}
                                    >
                                        <Form.Item
                                            label="Ttile"
                                            name="title"
                                        >
                                            <Input allowClear/>
                                        </Form.Item>
                                        <Form.Item
                                            label="Content"
                                            name="content"
                                        >
                                            <TextArea allowClear/>
                                        </Form.Item>
                                        <Form.Item style={{textAlign: "right"}}>
                                            <Button type="primary" htmlType="submit" loading={publishing}>
                                                Publish
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </Content>
                            </Layout>
                        </div>
                        <div>
                            <Layout style={{height: "100%"}}>
                                <Header style={{backgroundColor: "white"}}>
                                    <Row>
                                        <Col span={12}>
                                            <Button
                                                type="text"
                                                icon={<AiOutlineArrowLeft/>}
                                                onClick={() => {
                                                    setSelectedPost(null)
                                                    commentFormRef.current?.resetFields()
                                                    carouselRef.current.goTo(0)
                                                }}
                                            >
                                                Go Back
                                            </Button>
                                        </Col>
                                        <Col span={12}></Col>
                                    </Row>
                                </Header>
                                <Divider style={{margin: "0 0 0 0"}}/>
                                <Content style={{backgroundColor: "white"}}>
                                    <Space
                                        direction="vertical"
                                        style={{width: "100%"}}
                                    >
                                        <Typography>
                                            <Title>{selectedPost?.title}</Title>
                                            <Paragraph>{selectedPost?.content}</Paragraph>
                                        </Typography>
                                        <List
                                            loading={commentListLoading}
                                            locale={{emptyText: <span></span>}}
                                            bordered
                                            dataSource={comments}
                                            renderItem={comment => (
                                                <List.Item
                                                    actions={renderCommentActions(comment)}
                                                >
                                                    <List.Item.Meta
                                                        title={comment.username}
                                                        description={comment.status + " " + comment.createdTime}
                                                    />
                                                    {comment.content}
                                                </List.Item>
                                            )}
                                            loadMore={
                                                <div
                                                    style={{
                                                        textAlign: 'center',
                                                        marginTop: 12,
                                                        height: 32,
                                                        lineHeight: '32px',
                                                    }}
                                                >
                                                    {
                                                        comments.length < commentTotalNumber ?
                                                            <Button onClick={() => loadMoreComments()}>
                                                                Load More
                                                            </Button> :
                                                            <Divider>No more Comments</Divider>
                                                    }
                                                </div>
                                            }
                                            style={{
                                                height: "24vh",
                                                overflow: "auto"
                                            }}
                                        />
                                        <Form
                                            name="commentForm"
                                            ref={commentFormRef}
                                            autoComplete="off"
                                            onFinish={publishComment}
                                        >
                                            <Form.Item
                                                name="comment"
                                            >
                                                <TextArea
                                                    placeholder="happy to see your comment"
                                                    allowClear
                                                />
                                            </Form.Item>
                                            <Form.Item style={{textAlign: "right"}}>
                                                <Button type="primary" htmlType="submit" loading={commentPublishing}>
                                                    Publish Comment
                                                </Button>
                                            </Form.Item>
                                        </Form>
                                    </Space>
                                </Content>
                            </Layout>
                        </div>
                    </Carousel>
                </Col>
                <Col span={2}></Col>
            </Row>
        </>
    )
}