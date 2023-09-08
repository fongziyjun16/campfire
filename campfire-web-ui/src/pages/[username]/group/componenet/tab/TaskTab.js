import {useEffect, useRef, useState} from "react";
import {Button, Card, Carousel, Col, DatePicker, Divider, Form, Input, List,
    Modal, Popconfirm, Radio, Row, Space, Spin, Tooltip, Typography} from "antd";
import dayjs from "dayjs";
import {Calendar, Views, dayjsLocalizer} from "react-big-calendar";
import 'react-big-calendar/lib/css/react-big-calendar.css'
import {AiOutlineLeft, AiOutlineRight} from "react-icons/ai";
import {RiPlayListAddFill} from "react-icons/ri";
import {useSelector} from "react-redux";
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";
import axios from "axios";
import {BiRefresh} from "react-icons/bi";

const localizer = dayjsLocalizer(dayjs)
const {TextArea} = Input
const {RangePicker} = DatePicker
const {Title, Paragraph} = Typography

export default function TaskTab({notificationApi, groupId, leaderId}) {

    const accountId = useSelector(selectAccountId)
    const carouselRef = useRef(null)
    const [datePickerDisable, setDatePickerDisable] = useState(false)
    const [datePickerFormat, setDatePickerFormat] = useState("YYYY-MM")
    const [date, setDate] = useState(dayjs())
    const [view, setView] = useState(Views.MONTH)
    const [eventsLoading, setEventsLoading] = useState(false)
    const [events, setEvents] = useState([])
    const [isAddTaskModalOpen, setIsAddTaskModalOpen] = useState(false)
    const addTaskFormRef = useRef(null)
    const [addingTask, setAddingTask] = useState(false)
    const [addingAllDayTask, setAddingAllDayTask] = useState(false)
    const [selectedEvent, setSelectedEvent] = useState(null)
    const [taskContentLoading, setTaskContentLoading] = useState(false)
    const [deleteTaskLoading, setDeleteTaskLoading] = useState(false)
    const [markCompleteFormLoading, setMarkCompleteFormLoading] = useState(false)
    const [markCompleteForm, setMarkCompleteForm] = useState(<></>)
    const [markCompleteWaiting, setMarkCompleteWaiting] = useState(false)
    const [deleteMarkCompleteWaiting, setDeleteMarkCompleteWaiting] = useState(false)
    const [taskCompletionsLoading, setTaskCompletionLoading] = useState(false)
    const [taskCompletions, setTaskCompletions] = useState([])
    const [waitingTaskCompletions, setWaitingTaskCompletions] = useState([])

    useEffect(() => {
        loadEvents(date.month() + 1)
    }, []);

    useEffect(() => {
        setDatePickerFormat(view === Views.DAY ? "YYYY-MM-DD" : "YYYY-MM")
    }, [view]);

    const loadEvents = (month) => {
        setEventsLoading(true)
        axios.get(
            "/api/task/group/" + groupId,
            {
                params: {
                    month: month
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {data: loadingEvents} = resp.data
                // console.log(loadingEvents)
                setEvents([...loadingEvents.map(loadingEvent => ({
                    ...loadingEvent,
                    start: new Date(loadingEvent.startDate + " " + loadingEvent.startTime),
                    end: new Date(loadingEvent.endDate + " " + loadingEvent.endTime),
                    resource: loadingEvent.id
                }))])
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Events Error. Try again later"
            })
        }).finally(() => {
            setEventsLoading(false)
        })
    }

    const renderToolbar = () => {
        return (
            <Row style={{paddingBottom: "5px"}}>
                <Col span={8}>
                    <Button
                        onClick={() => {
                            if (dayjs().month() !== date.month()) {
                                loadEvents(dayjs().month() + 1)
                            }
                            setDate(dayjs());
                        }}
                    >
                        Today
                    </Button>
                </Col>
                <Col span={8} style={{display: "flex", alignSelf: "center", justifyContent: "center"}}>
                    <Tooltip placement="left" title="Last Month">
                        <Button
                            icon={<AiOutlineLeft/>}
                            onClick={() => {
                                const curr = date.subtract(1, 'month')
                                loadEvents(curr.month() + 1)
                                setDate(curr)
                            }}
                        ></Button>
                    </Tooltip>
                    <DatePicker
                        allowClear={false}
                        inputReadOnly={true}
                        disabled={datePickerDisable}
                        value={date}
                        format={datePickerFormat}
                        onChange={(currDate, dateString) => {
                            if (currDate) {
                                if (currDate.month() !== date.month()) {
                                    loadEvents(currDate.month() + 1)
                                }
                                setDate(currDate)
                            }
                        }}
                        style={{
                            width: 200
                        }}
                    />
                    <Tooltip placement="right" title="Next Month">
                        <Button
                            icon={<AiOutlineRight/>}
                            onClick={() => {
                                const curr = date.add(1, 'month')
                                loadEvents(curr.month() + 1)
                                setDate(curr)
                            }}
                        ></Button>
                    </Tooltip>
                </Col>
                <Col span={8} style={{display: "flex", alignSelf: "center", justifyContent: "flex-end"}}>
                    <Radio.Group
                        value={view}
                        buttonStyle="solid"
                        onChange={(e) => {
                            const value = e.target.value
                            // console.log(value)
                            if (value !== view) {
                                setView(value)
                            }
                        }}
                    >
                        <Radio.Button value="month">Month</Radio.Button>
                        <Radio.Button value="day">Day</Radio.Button>
                    </Radio.Group>
                </Col>
            </Row>
        )
    }

    const selectEvent = async (event) => {
        // console.log(event)
        axios.get(
            "/api/task/" + event.id
        ).then(resp => {
            if (resp.data?.data) {
                event["content"] = resp.data.data
                setSelectedEvent(event)
                renderMarkComplete(event)
                loadTaskCompletions(event.id)
                carouselRef.current.goTo(1)
            } else {
                notificationApi.error({
                    message: "No Task Content"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Task Content Error. Try again later"
            })
        }).finally(() => {

        })
    }

    const addTask = (values) => {
        const {title, content, timeRange} = values
        if (title && title.trim().length > 0 && content && content.trim().length > 0 && timeRange) {
            setAddingTask(true)
            axios.post(
                "/api/task/group",
                {
                    ownerType: "GROUP",
                    ownerId: groupId,
                    title: title,
                    content: content,
                    startDate: timeRange[0].format("YYYY-MM-DD"),
                    startTime: timeRange[0].format("HH:mm:ss"),
                    endDate: timeRange[1].format("YYYY-MM-DD"),
                    endTime: timeRange[1].format("HH:mm:ss"),
                },
                {
                    headers: {
                        "Content-Type": "application/json"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    notificationApi.success({
                        message: "Add task successfully."
                    })
                    setAddingAllDayTask(false)
                    setIsAddTaskModalOpen(false)
                    const curr = dayjs(new Date(timeRange[0].format("YYYY-MM-DD")))
                    loadEvents(curr.month() + 1);
                    setDate(curr)
                } else {
                    notificationApi.error({
                        message: "Fail to Add Task. Incorrect parameters"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Add Task Error. Try again later"
                })
            }).finally(() => {
                setAddingTask(false)
            })
        } else {
            notificationApi.warning({
                message: "Input all Required Items"
            })
        }
    }

    const deleteTask = () => {
        // console.log(selectedEvent)
        if (selectedEvent) {
            setDeleteTaskLoading(true);
            axios.delete(
                "/api/task/" + selectedEvent.id
            ).then(resp => {
                if (resp.data?.code === 200) {
                    carouselRef.current.goTo(0)
                    loadEvents(date.month() + 1)
                } else {
                    notificationApi.error({
                        message: "Fail to Delete Task Error.",
                        description: "Wrong Parameters"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Delete Task Error. Try again later"
                })
            }).finally(() => {
                setDeleteTaskLoading(false)
            })
        }
    }

    const renderMarkComplete = (event) => {
        setMarkCompleteFormLoading(true)
        axios(
            "/api/task/complete/" + event.id
        ).then(resp => {
            if (resp.data?.code === 200) {
                const {comment, completedTime} = resp.data.data
                setMarkCompleteForm(
                    <Space direction="vertical">
                        <span style={{fontStyle: "italic", fontSize: "20px"}}>{comment ? comment : "no comment"}</span>
                        <Space direction="vertical">
                            <div>Marked Completion. {completedTime}</div>
                            <Button
                                type="primary" danger size="small"
                                loading={deleteMarkCompleteWaiting}
                                onClick={() => deleteMarkComplete(event)}
                            >
                                Delete Mark Complete
                            </Button>
                        </Space>
                    </Space>
                )
            } else {
                if (dayjs().isBetween(dayjs(event.start), dayjs(event.end))) {
                    setMarkCompleteForm(
                        <>
                            <Form
                                autoComplete="off"
                                onFinish={(values) => {
                                    values["event"] = event
                                    markComplete(values)
                                }}
                            >
                                <Form.Item name="comment">
                                    <TextArea placeholder="feel free to write down your comment"/>
                                </Form.Item>
                                <Form.Item style={{display: "flex", justifyContent: "flex-end"}}>
                                    <Button type="primary" htmlType="submit" loading={markCompleteWaiting}>
                                        Mark Complete
                                    </Button>
                                </Form.Item>
                            </Form>
                        </>
                    )
                } else {
                    setMarkCompleteForm(
                        <>
                            <span>This task cannot be marked completed</span>
                        </>
                    );
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Task Completion Information Error. Try again later"
            })
        }).finally(() => {
            setMarkCompleteFormLoading(false)
        })
    }

    const markComplete = (values) => {
        const {event, comment} = values
        if (event) {
            setMarkCompleteWaiting(true);
            axios.post(
                "/api/task/complete",
                {
                    taskId: event.id,
                    comment: comment?.trim()
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    renderMarkComplete(event)
                    if (accountId === leaderId) {
                        loadTaskCompletions(event.id)
                    }
                } else {
                    notificationApi.error({
                        message: "Fail to Mark Task Complete.",
                        description: "Wrong Parameters"
                    })
                }
            }).catch(err => {
                console.log(err)
                notificationApi.error({
                    message: "Mark Task Complete Error. Try again later"
                })
            }).finally(() => {
                setMarkCompleteWaiting(false)
            })
        }
    }

    const deleteMarkComplete = (event) => {
        setDeleteMarkCompleteWaiting(true)
        axios.delete(
            "/api/task/complete/" + event.id
        ).then(resp => {
            if (resp.data?.code === 200) {
                renderMarkComplete(event)
                if (accountId === leaderId) {
                    loadTaskCompletions(event.id)
                }
            } else {
                notificationApi.error({
                    message: "Fail to Delete Mark Complete Error.",
                    description: "Wrong Parameters"
                })
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Delete Mark Complete Error. Try again later"
            })
        }).finally(() => {
            setDeleteMarkCompleteWaiting(false)
        })
    }

    const loadTaskCompletions = async (taskId) => {
        setTaskCompletionLoading(true)
        try {
            const groupMembersResp = await axios.get("/api/group/joinings/" + groupId)
            const taskCompletionsResp = await axios.get("/api/task/completions/" + taskId)
            if (groupMembersResp.data?.data && taskCompletionsResp.data?.data) {
                waitingTaskCompletions.splice(0, waitingTaskCompletions.length)
                groupMembersResp.data.data.forEach(groupMember => {
                    if (taskCompletionsResp.data.data.findIndex(taskCompletion => taskCompletion.accountId === groupMember.id) === -1) {
                        waitingTaskCompletions.push(groupMember)
                    }
                })
                setWaitingTaskCompletions([...waitingTaskCompletions])
                setTaskCompletions([...taskCompletionsResp.data.data])
            } else {
                notificationApi.error({
                    message: "Fail to load group task completions",
                    description: "Wrong parameters"
                })
            }
        } catch (e) {
            console.log(e)
            notificationApi.error({
                message: "Load Group Task Completions Error"
            })
        } finally {
            setTaskCompletionLoading(false)
        }
    }

    return (
        <>
            <Row>
                <Col span={1}></Col>
                <Col span={22} style={{height: "100%", overflow: "auto"}}>
                    <Carousel
                        ref={carouselRef}
                        dots={null}
                        infinite={false}
                        effect="fade"
                    >
                        <div>
                            <Spin spinning={eventsLoading}>
                                <Space
                                    align="center"
                                    style={{
                                        width: "100%",
                                        display: "flex", flexDirection: "column", justifyContent: "center"
                                    }}
                                >
                                    <Card
                                        title="Group Task Panel"
                                        extra={
                                            accountId === leaderId ?
                                                <Button
                                                    icon={<RiPlayListAddFill/>}
                                                    onClick={() => setIsAddTaskModalOpen(true)}
                                                >
                                                    Add Task
                                                </Button> : <></>
                                        }
                                    >
                                        <Calendar
                                            localizer={localizer}
                                            date={date.toDate()}
                                            onNavigate={(date) => {
                                                setDate(dayjs(date))
                                            }}
                                            view={view}
                                            onView={(view) => {
                                                setView(view)
                                            }}
                                            components={{
                                                toolbar: renderToolbar
                                            }}
                                            selectable
                                            onSelectSlot={(slotInfo) => {
                                                setDate(dayjs(slotInfo.start))
                                            }}
                                            dayPropGetter={(currentDate) =>
                                                (dayjs(currentDate).isSame(date, "day")) &&
                                                ({
                                                    style: {
                                                        backgroundColor: "#fffbe6"
                                                    }
                                                })
                                            }
                                            events={events}
                                            popup={true}
                                            onSelectEvent={selectEvent}
                                            style={{
                                                height: 500,
                                                width: 900
                                            }}
                                        />
                                    </Card>
                                </Space>
                            </Spin>
                        </div>
                        <div>
                            <Card style={{width: "99%"}}>
                                <Spin spinning={taskContentLoading}>
                                    <Space direction="vertical" style={{width: "100%"}}>
                                        <Row>
                                            <Col span={12}>
                                                <Button
                                                    icon={<AiOutlineLeft/>}
                                                    onClick={() => {
                                                        carouselRef.current.goTo(0)
                                                        setSelectedEvent(null)
                                                    }}
                                                >
                                                    Back to Task Panel
                                                </Button>
                                            </Col>
                                            <Col span={12} style={{
                                                display: "flex",
                                                alignSelf: "center",
                                                justifyContent: "flex-end"
                                            }}>
                                                {
                                                    accountId === leaderId ?
                                                        <Popconfirm
                                                            title="Delete the task"
                                                            description="Are you sure to delete this task?"
                                                            onConfirm={deleteTask}
                                                            okText="Yes"
                                                            cancelText="No"
                                                        >
                                                            <Button type="primary" danger loading={deleteTaskLoading}>
                                                                Delete This Task
                                                            </Button>
                                                        </Popconfirm> : <></>
                                                }
                                            </Col>
                                        </Row>
                                        <Typography>
                                            <Title>{selectedEvent?.title}</Title>
                                            <Paragraph>
                                                {selectedEvent ?
                                                    (selectedEvent?.ownerType === "GROUP" ? "Group Task" : "Personal Task") :
                                                    ""
                                                }
                                            </Paragraph>
                                            <Paragraph>
                                                {selectedEvent ?
                                                    ("Time Range: " +
                                                        dayjs(selectedEvent.start).format("YYYY-MM-DD HH:mm:ss") + " - " +
                                                        dayjs(selectedEvent.end).format("YYYY-MM-DD HH:mm:ss")) :
                                                    ""
                                                }
                                            </Paragraph>
                                            <Paragraph>{selectedEvent?.content}</Paragraph>
                                        </Typography>
                                        <Divider/>
                                        <Spin spinning={markCompleteFormLoading}>
                                            {markCompleteForm}
                                        </Spin>
                                        <Divider/>
                                        {accountId === leaderId ?
                                            <>
                                                <Button icon={<BiRefresh/>}
                                                        onClick={() => loadTaskCompletions(selectedEvent.id)}/>
                                                <Row justify="space-around" style={{width: "100%", height: "300px"}}>
                                                    <Col span={11} style={{
                                                        display: "flex", justifyContent: "center",
                                                        border: "1px solid black",
                                                        borderRadius: "5px",
                                                        overflow: "auto"
                                                    }}>
                                                        <List
                                                            header={<div style={{textAlign: "center"}}>Marked
                                                                Completion</div>}
                                                            loading={taskCompletionsLoading}
                                                            dataSource={taskCompletions}
                                                            renderItem={taskCompletion => (
                                                                <Tooltip title={taskCompletion.comment}>
                                                                    <List.Item>
                                                                        <List.Item.Meta
                                                                            title={taskCompletion.username}
                                                                            description={taskCompletion.completedTime}
                                                                        />
                                                                    </List.Item>
                                                                </Tooltip>
                                                            )}
                                                            style={{width: "98%"}}
                                                        />
                                                    </Col>
                                                    <Col span={11} style={{
                                                        display: "flex", justifyContent: "center",
                                                        border: "1px solid black",
                                                        borderRadius: "5px",
                                                        overflow: "auto"
                                                    }}>
                                                        <List
                                                            header={<div style={{textAlign: "center"}}>
                                                                Waiting for Marking Completion</div>}
                                                            loading={taskCompletionsLoading}
                                                            dataSource={waitingTaskCompletions}
                                                            renderItem={waitingTaskCompletion => (
                                                                <List.Item>
                                                                    <List.Item.Meta
                                                                        title={waitingTaskCompletion.username}
                                                                    />
                                                                </List.Item>
                                                            )}
                                                            style={{width: "98%"}}
                                                        />
                                                    </Col>
                                                </Row>
                                            </> : <></>}
                                    </Space>
                                </Spin>
                            </Card>
                        </div>
                    </Carousel>
                </Col>
                <Col span={1}></Col>
            </Row>
            <Modal
                title="Add Task"
                open={isAddTaskModalOpen}
                footer={null}
                destroyOnClose={true}
                closeIcon={false}
            >
                <Form
                    name="addTaskForm"
                    ref={addTaskFormRef}
                    autoComplete="off"
                    onFinish={addTask}
                    labelCol={{
                        span: 5,
                    }}
                    wrapperCol={{
                        span: 19,
                    }}
                >
                    <Form.Item
                        name="title"
                        label="Title"
                        required={true}
                    >
                        <Input allowClear/>
                    </Form.Item>
                    <Form.Item
                        name="content"
                        label="Content"
                        required={true}
                    >
                        <TextArea allowClear/>
                    </Form.Item>
                    <Form.Item
                        name="timeRange"
                        label="Time Range"
                        required={!addingAllDayTask}
                    >
                        <RangePicker disabled={addingAllDayTask} showTime/>
                    </Form.Item>
                    <Form.Item style={{display: "flex", alignSelf: "center", justifyContent: "flex-end"}}>
                        <Space>
                            <Button onClick={() => {
                                setAddingAllDayTask(false)
                                setIsAddTaskModalOpen(false)
                            }}>
                                Cancel
                            </Button>
                            <Button type="primary" htmlType="submit" loading={addingTask}>
                                Add
                            </Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    )
}