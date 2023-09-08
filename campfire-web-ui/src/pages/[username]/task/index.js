import {useSelector} from "react-redux";
import MainLayout from "@/pages/[username]/layout";
import {
    Button,
    Card,
    Col,
    DatePicker, Divider,
    Drawer,
    Form,
    Input,
    notification, Popconfirm,
    Radio,
    Row,
    Space,
    Tooltip,
    Typography
} from "antd";
import {Calendar, Views, dayjsLocalizer} from "react-big-calendar";
import 'react-big-calendar/lib/css/react-big-calendar.css'
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";
import {MdFormatListBulletedAdd} from "react-icons/md";
import {useEffect, useRef, useState} from "react";
import axios from "axios";
import dayjs from "dayjs";
import {AiOutlineLeft, AiOutlineRight} from "react-icons/ai";

const {TextArea} = Input
const {RangePicker} = DatePicker
const localizer = dayjsLocalizer(dayjs)
const {Title, Paragraph} = Typography

function TaskPage() {

    const accountId = useSelector(selectAccountId)
    const [notificationApi, notificationContextHolder] = notification.useNotification()
    const [drawerOpen, setDrawerOpen] = useState(false)
    const [drawerMode, setDrawerMode] = useState("addTask")
    const addTaskFormRef = useRef(null)
    const [addingTask, setAddingTask] = useState(false)
    const [deletingTask, setDeletingTask] = useState(false)
    const [datePickerFormat, setDatePickerFormat] = useState("YYYY-MM")
    const [date, setDate] = useState(dayjs())
    const [view, setView] = useState(Views.MONTH)
    const [events, setEvents] = useState([])
    const [selectedEvent, setSelectedEvent] = useState(null)
    const [taskCompletion, setTaskCompletion] = useState(null)
    const markCompleteFormRef = useRef(null)
    const [markCompleteWaiting, setMarkCompleteWaiting] = useState(false)
    const [deleteMarkCompleteWaiting, setDeleteMarkCompleteWaiting] = useState(false)

    useEffect(() => {
        loadEvents(dayjs().month() + 1)
    }, []);

    const loadEvents = (month) => {
        axios.get(
            "/api/task/person",
            {
                params: {
                    month: month
                }
            }
        ).then(resp => {
            if (resp.data) {
                const {data: personalTasks} = resp.data
                setEvents([...personalTasks.map(personalTask => ({
                    ...personalTask,
                    start: new Date(personalTask.startDate + " " + personalTask.startTime),
                    end: new Date(personalTask.endDate + " " + personalTask.endTime),
                }))])
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Personal Tasks Error. Try again later"
            })
        })
    }

    const selectEvent = (event) => {
        // console.log(event)
        setSelectedEvent(event)
        markCompleteFormRef.current?.resetFields()
        setDrawerMode("viewTask")
        setDrawerOpen(true)
        axios.get(
            "/api/task/" + event.id
        ).then(resp => {
            if (resp.data?.data) {
                event["content"] = resp.data.data
                setSelectedEvent({
                    ...event
                })
                loadTaskCompletion(event.id)
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Get Task Content Error. Try again later"
            })
        })
    }

    const addTask = (values) => {
        const {title, content, timeRange} = values
        if (title && title.trim().length > 0 && content && content.trim().length > 0 && timeRange) {
            setAddingTask(true)
            axios.post(
                "/api/task/person",
                {
                    ownerType: "PERSON",
                    ownerId: accountId,
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
                    setDrawerOpen(false)
                    addTaskFormRef.current?.resetFields()
                    loadEvents(dayjs(timeRange[0]).month() + 1)
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

    const deleteTask = (taskId) => {
        setDeletingTask(true);
        axios.delete(
            "/api/task/" + taskId
        ).then(resp => {
            if (resp.data?.code === 200) {
                loadEvents(date.month() + 1)
                setDrawerOpen(false)
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
            setDeletingTask(false)
        })
    }

    const loadTaskCompletion = (taskId) => {
        axios.get(
            "/api/task/complete/" + taskId
        ).then(resp => {
            if (resp.data?.code === 200) {
                const {data} = resp.data
                setTaskCompletion(data)
            } else {
                setTaskCompletion(null)
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Task Completion Error. Try again later"
            })
        })
    }

    const markComplete = (values) => {
        const {comment} = values
        if (selectedEvent) {
            setMarkCompleteWaiting(true);
            axios.post(
                "/api/task/complete",
                {
                    taskId: selectedEvent.id,
                    comment: comment?.trim()
                },
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            ).then(resp => {
                if (resp.data?.code === 200) {
                    loadTaskCompletion(selectedEvent.id)
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

    const deleteMarkComplete = (taskId) => {
        setDeleteMarkCompleteWaiting(true)
        axios.delete(
            "/api/task/complete/" + taskId
        ).then(resp => {
            if (resp.data?.code === 200) {
                loadTaskCompletion(taskId)
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

    return (
        <>
            {notificationContextHolder}
            <Row style={{paddingTop: "10px"}}>
                <Col span={3}></Col>
                <Col span={18}>
                    <Card
                        title="Personal Task Panel"
                        extra={[
                            <Button
                                icon={<MdFormatListBulletedAdd/>}
                                key={accountId + "_task_add_task_btn"}
                                onClick={() => {
                                    setDrawerMode("addTask")
                                    setDrawerOpen(true)
                                    addTaskFormRef.current?.resetFields()
                                }}
                            >
                                Add Personal Task
                            </Button>
                        ]}
                    >
                        <div style={{display: "flex", justifyContent: "center"}}>
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
                                    toolbar: () => (
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
                                            <Col span={8} style={{
                                                display: "flex",
                                                alignSelf: "center",
                                                justifyContent: "center"
                                            }}>
                                                <Tooltip placement="left" title="Last Month">
                                                    <Button
                                                        icon={<AiOutlineLeft/>}
                                                        onClick={() => {
                                                            const curr = date.subtract(1, 'month')
                                                            loadEvents(curr.month() + 1)
                                                            setDate(curr)
                                                        }}
                                                    />
                                                </Tooltip>
                                                <DatePicker
                                                    allowClear={false}
                                                    inputReadOnly={true}
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
                                                    style={{width: 200}}
                                                />
                                                <Tooltip placement="right" title="Next Month">
                                                    <Button
                                                        icon={<AiOutlineRight/>}
                                                        onClick={() => {
                                                            const curr = date.add(1, 'month')
                                                            loadEvents(curr.month() + 1)
                                                            setDate(curr)
                                                        }}
                                                    />
                                                </Tooltip>
                                            </Col>
                                            <Col span={8} style={{
                                                display: "flex",
                                                alignSelf: "center",
                                                justifyContent: "flex-end"
                                            }}>
                                                <Radio.Group
                                                    value={view}
                                                    buttonStyle="solid"
                                                    onChange={(e) => {
                                                        const value = e.target.value
                                                        if (value !== view) {
                                                            setDatePickerFormat(value === Views.MONTH ? "YYYY-MM" : "YYYY-MM-DD")
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
                                    width: 900,
                                }}
                            />
                        </div>
                    </Card>
                </Col>
                <Col span={3}></Col>
            </Row>
            <Drawer
                open={drawerOpen}
                title={drawerMode === "addTask" ? "Add Task" : "View Task"}
                width={drawerMode === "addTask" ? 600 : 800}
                onClose={() => {
                    setDrawerOpen(false)
                    addTaskFormRef.current?.resetFields()
                }}
            >
                {drawerMode === "addTask" ?
                    <>
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
                                required={true}
                            >
                                <RangePicker showTime/>
                            </Form.Item>
                            <Form.Item style={{display: "flex", alignSelf: "center", justifyContent: "flex-end"}}>
                                <Button type="primary" htmlType="submit" loading={addingTask}>
                                    Add
                                </Button>
                            </Form.Item>
                        </Form>
                    </> :
                    <>
                        <Space direction="vertical" style={{width: "100%"}}>
                            <Row>
                                <Col span={24} style={{display: "flex", justifyContent: "flex-end"}}>
                                    <Popconfirm
                                        title="Delete the task"
                                        description="Are you sure to delete this task?"
                                        onConfirm={() => deleteTask(selectedEvent.id)}
                                        okText="Yes"
                                        cancelText="No"
                                    >
                                        <Button type="primary" danger loading={deletingTask}>
                                            Delete This Task
                                        </Button>
                                    </Popconfirm>

                                </Col>
                            </Row>
                            <Typography>
                                <Title>{selectedEvent?.title}</Title>
                                <Paragraph>{selectedEvent?.content}</Paragraph>
                            </Typography>
                            <Divider/>
                            {
                                taskCompletion ?
                                    <>
                                        <Space direction="vertical">
                                            <span style={{fontStyle: "italic", fontSize: "20px"}}>
                                                {taskCompletion.comment ? taskCompletion.comment : "no comment"}
                                            </span>
                                            <Space direction="vertical">
                                                <div>Marked Completion. {taskCompletion.completedTime}</div>
                                                <Button
                                                    type="primary" danger size="small"
                                                    loading={deleteMarkCompleteWaiting}
                                                    onClick={() => deleteMarkComplete(taskCompletion.taskId)}
                                                >
                                                    Delete Mark Complete
                                                </Button>
                                            </Space>
                                        </Space>
                                    </> :
                                    (
                                        dayjs().isBetween(dayjs(selectedEvent.start), dayjs(selectedEvent.end)) ?
                                            <>
                                                <Form
                                                    ref={markCompleteFormRef}
                                                    autoComplete="off"
                                                    onFinish={markComplete}
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
                                            </> :
                                            <>
                                                <span>This task cannot be marked completed</span>
                                            </>
                                    )
                            }
                        </Space>
                    </>
                }
            </Drawer>
        </>
    )
}

TaskPage.pageLayout = MainLayout

export default TaskPage