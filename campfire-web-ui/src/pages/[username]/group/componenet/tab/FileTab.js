import {Button, Card, Col, Divider, Dropdown, Layout, Popconfirm, Row, Space, Spin, Upload} from "antd";
import {useEffect, useState} from "react";
import {useSelector} from "react-redux";
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";
import {AiOutlineDelete, AiOutlineDownload, AiOutlineUpload} from "react-icons/ai";
import {BiRefresh} from "react-icons/bi";
import {SlOptions} from "react-icons/sl";
import axios from "axios";
import FileSaver from 'file-saver';

const {Header, Content} = Layout
const { Meta } = Card;

export default function FileTab({notificationApi, groupId, leaderId}) {

    const accountId = useSelector(selectAccountId)
    const [hasDirectory, setHasDirectory] = useState(false)
    const [groupDirectoryId, setGroupDirectoryId] = useState("")
    const [createGroupDirectoryLoading, setCreateGroupDirectoryLoading] = useState(false)
    const [totalSize, setTotalSize] = useState("64")
    const [remainingSize, setRemainingSize] = useState("64")
    const [fileListLoading, setFileListLoading] = useState(false)
    const [fileTotalNumber, setFileTotalNumber] = useState(0)
    const [files, setFiles] = useState([])
    const [uploadFiles, setUploadFiles] = useState([])
    const [uploading, setUploading] = useState(false)

    useEffect(() => {
        loadGroupDirectory()
    }, []);

    const createGroupDirectory = () => {
        setCreateGroupDirectoryLoading(true)
        axios.post(
            "/api/upload/group_directory",
            {
                groupId: groupId
            },
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                setHasDirectory(true)
                loadFiles()
                notificationApi.success({
                    message: "Create Group Directory Successfully"
                })
            } else {
                notificationApi.error({
                    message: "Fail to Create Group Directory."
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Create Group Directory Error. Try again later"
            })
        }).finally(() => {
            setCreateGroupDirectoryLoading(false)
        })
    }

    const loadGroupDirectory = () => {
        setCreateGroupDirectoryLoading(true)
        axios.get(
            "/api/file/info/directory/" + groupId
        ).then(resp => {
            if (resp.data?.code === 200) {
                const {data: groupDirInfo} = resp.data
                setTotalSize(groupDirInfo.maxSize)
                setRemainingSize(groupDirInfo.availableSize)
                setHasDirectory(true)
                setGroupDirectoryId(groupDirInfo.id)
                clearFiles()
                loadFiles(groupDirInfo.id)
            } else {
                // notificationApi.error({
                //     message: "Get Group Directory Info Error"
                // })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Get Group Directory Info Error"
            })
        }).finally(() => {
            setCreateGroupDirectoryLoading(false)
        })
    }

    const clearFiles = () => {
        setFileTotalNumber(0)
        files.splice(0, files.length)
        setFiles([...files])
    }

    const loadFiles = (groupDirectoryId) => {
        if (groupDirectoryId !== "") {
            setFileListLoading(true);
            axios.get(
                "/api/file/info/files/" + groupDirectoryId,
                {
                    params: {
                        size: 6,
                        havingSize: files.length
                    }
                }
            ).then(resp => {
                if (resp.data?.data) {
                    const {total, groupFileHeads: newFiles} = resp.data.data
                    setFileTotalNumber(total)
                    if (files.length === 0) {
                        setFiles([...newFiles])
                    } else {
                        const arr = [...newFiles]
                        arr.forEach(element => {
                            if (files.findIndex(file => file.id === element.id) === -1) {
                                files.push(element);
                            }
                        })
                        setFiles([...files])
                    }
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Load File Info Error. Try again later"
                })
            }).finally(() => {
                setFileListLoading(false)
            })
        }
    }

    const loadMoreFile = () => {
        if (files.length < fileTotalNumber) {
            loadFiles(groupDirectoryId)
        }
    }

    const renderFileCardActions = (file) => {
        const actions = []
        if (accountId === leaderId) { // group leader
            actions.push(
                <Dropdown
                    menu={{
                        items: [
                            {
                                key: "open",
                                label: (
                                    <Button
                                        key={file.id + "_change_2_open_btn"}
                                        onClick={() => changeFileStatus(file, "OPEN")}
                                    >
                                        Change to Open
                                    </Button>
                                )
                            },
                            {
                                key: "frozen",
                                label: (
                                    <Button
                                        key={file.id + "_change_2_frozen_btn"}
                                        onClick={() => changeFileStatus(file, "FROZEN")}
                                    >
                                        Change to Frozen
                                    </Button>
                                )
                            }
                        ]
                    }}
                    arrow
                >
                    <Button icon={<SlOptions/>}></Button>
                </Dropdown>,
                <Popconfirm
                    title="Delete the file"
                    description="Are you sure to delete this file?"
                    onConfirm={() => deleteFile(file)}
                    okText="Yes"
                    cancelText="No"
                >
                    <Button icon={<AiOutlineDelete/>}>
                    </Button>
                </Popconfirm>,
                <Button
                    icon={<AiOutlineDownload/>}
                    onClick={() => downloadFile(file)}
                >
                </Button>
            )
        } else { // group member
            if (file.status === "OPEN") {
                actions.push(
                    <Button icon={<AiOutlineDownload/>}></Button>
                )
            }
        }
        return actions;
    }

    const downloadFile = (file) => {
        axios.get(
            "/api/download/group-file",
            {
                params: {
                    fileId: file.id
                },
                responseType: "blob"
            }
        ).then(resp => {
            FileSaver.saveAs(resp.data, file.displayName)
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Download File Error. Try again later"
            })
        })
    }

    const deleteFile = (file) => {
        axios.delete(
            "/api/file/info/" + file.id
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Delete File Successfully"
                })
                const index = files.findIndex(element => element.id === file.id)
                files.splice(index, 1)
                setFileTotalNumber(files.length)
                setFiles([...files])
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Delete File Error. Try again later"
            })
        })
    }

    const changeFileStatus = (file, newStatus) => {
        const currentFile = files.find(element => element.id === file.id)
        if (currentFile.status !== newStatus) {
            axios.put(
                "/api/file/info/status",
                {
                    groupFileId: file.id,
                    status: newStatus
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    notificationApi.success({
                        message: "Update Group File Successfully",
                    })
                    currentFile.status = newStatus
                    setFiles([...files])
                } else {
                    notificationApi.error({
                        message: "Update Group File Error",
                        description: "Wrong Parameters or Insufficient Authorities"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Update Group File Error. Try again later"
                })
            })
        }
    }

    const handleUpload = () => {
        // console.log(uploadFiles)
        const formData = new FormData()
        formData.append("groupId", groupId)
        uploadFiles.forEach(uploadFile => formData.append("files", uploadFile))
        setUploading(true)
        axios.post(
            "/api/upload/group_files",
            formData,
            {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            }
        ).then(resp => {
            if (resp.data?.code === 200) {
                notificationApi.success({
                    message: "Upload Files Successfully"
                })
                setUploadFiles([])
                clearFiles()
                loadFiles(groupDirectoryId)
            } else {
                notificationApi.error({
                    message: "Upload Files Error",
                    description: "No available space or Wrong Parameters"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Upload Files Error. Try again later"
            })
        }).finally(() => {
            setUploading(false)
        })
    }

    return (
        <>
            <Row>
                <Col span={3}></Col>
                <Col span={18}>
                    {
                        !hasDirectory ?
                            <>
                                {
                                    accountId === leaderId ?
                                        <>
                                            <Button
                                                type="primary"
                                                loading={createGroupDirectoryLoading}
                                                onClick={createGroupDirectory}
                                            >
                                                Create Group Directory
                                            </Button>
                                        </> :
                                        <>
                                            <h2>Wait for Group Leader to Create Group Directory</h2>
                                        </>
                                }
                            </> :
                            <Layout style={{height: "100%"}}>
                                <Header style={{backgroundColor: "white"}}>
                                    <Row>
                                        <Col span={12}>
                                            <span style={{fontWeight: "bold", fontSize: "26px"}}>Group Directory</span>
                                        </Col>
                                        <Col span={12}
                                             style={{display: "flex", justifyContent: "flex-end", alignSelf: "center"}}>
                                            <Button
                                                icon={<BiRefresh/>}
                                                onClick={loadGroupDirectory}>
                                            </Button>
                                        </Col>
                                    </Row>
                                </Header>
                                <Divider style={{margin: "0 0 0 0"}}/>
                                <Content
                                    style={{
                                        height: "100%",
                                        overflow: "auto",
                                        backgroundColor: "white",
                                        paddingTop: "10px"
                                    }}
                                >
                                    <Space direction="vertical" align="center">
                                        <Spin spinning={fileListLoading}>
                                            <Space
                                                size={[8, 16]}
                                                wrap
                                                style={{
                                                    height: "50vh",
                                                    overflow: "auto"
                                                }}
                                            >
                                                {files.map(file => (
                                                    <Card
                                                        key={file.id + "_card"}
                                                        actions={renderFileCardActions(file)}
                                                    >
                                                        <Meta
                                                            title={file.displayName}
                                                            description={
                                                                <div>
                                                                    <span>Size: {file.size} Bytes</span> <br/>
                                                                    <span>Status: {file.status}</span> <br/>
                                                                    <span>{file.createdTime}</span> <br/>
                                                                    <span>Uploader: {file.username}</span>
                                                                </div>
                                                            }
                                                        />
                                                    </Card>
                                                ))}
                                            </Space>
                                            <div
                                                style={{
                                                    paddingTop: "10px",
                                                    display: "flex",
                                                    justifyContent: "flex-end",
                                                    alignSelf: "center"
                                                }}
                                            >
                                                {
                                                    files.length < fileTotalNumber ?
                                                        <>
                                                            <Button
                                                                onClick={loadMoreFile}
                                                            >
                                                                Load More Files
                                                            </Button>
                                                        </> :
                                                        <></>
                                                }
                                            </div>
                                            <div style={{paddingTop: "10px"}}>
                                                Total: {totalSize} Bytes / Remaining: {remainingSize} Bytes
                                            </div>
                                            <Divider style={{margin: "0 0 0 0"}}/>
                                            <div style={{paddingTop: "10px"}}>
                                                {
                                                    accountId === leaderId ?
                                                        <>
                                                            <Space direction="vertical">
                                                                <Upload
                                                                    name="file"
                                                                    fileList={uploadFiles}
                                                                    beforeUpload={file => {
                                                                        setUploadFiles([...uploadFiles, file])
                                                                        return false
                                                                    }}
                                                                    onRemove={file => {
                                                                        const index = uploadFiles.indexOf(file)
                                                                        const newUploadFiles = uploadFiles.slice()
                                                                        newUploadFiles.splice(index, 1)
                                                                        setUploadFiles(newUploadFiles)
                                                                    }}
                                                                >
                                                                    <Button icon={<AiOutlineUpload/>}>
                                                                        Select File
                                                                    </Button>
                                                                </Upload>
                                                                <Button
                                                                    type="primary"
                                                                    disabled={uploadFiles.length === 0}
                                                                    loading={uploading}
                                                                    onClick={handleUpload}
                                                                >
                                                                    {uploading ? 'Uploading' : 'Start Upload'}
                                                                </Button>
                                                            </Space>
                                                        </> :
                                                        <></>
                                                }
                                            </div>
                                        </Spin>
                                    </Space>
                                </Content>
                            </Layout>
                    }
                </Col>
                <Col span={3}></Col>
            </Row>
        </>
    )
}