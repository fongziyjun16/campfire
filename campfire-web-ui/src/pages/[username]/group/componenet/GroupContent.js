import {Button, Space, Tabs} from "antd";
import {useEffect, useState} from "react";
import PostTab from "@/pages/[username]/group/componenet/tab/PostTab";
import MemberTab from "@/pages/[username]/group/componenet/tab/MemberTab";
import TaskTab from "@/pages/[username]/group/componenet/tab/TaskTab";
import FileTab from "@/pages/[username]/group/componenet/tab/FileTab";
import {BsFillSignpostFill, BsPeopleFill} from "react-icons/bs";
import {AiFillFile} from "react-icons/ai";
import {MdAssignment, MdPlaylistAddCircle} from "react-icons/md";
import {useSelector} from "react-redux";
import {selectAccountId} from "@/lib/redux/slices/accountIdSlice";

export default function GroupContent({notificationApi, groupId, groupName, leaderId}) {

    const accountId = useSelector(selectAccountId)

    const [tabs, setTabs] = useState([
        {
            key: 'post',
            label: (
                <Space>
                    <BsFillSignpostFill/>
                    Post
                </Space>
            ),
            children: (
                <PostTab
                    notificationApi={notificationApi}
                    groupId={groupId}
                    leaderId={leaderId}
                />
            ) ,
        },
        {
            key: 'member',
            label: (
                <Space>
                    <BsPeopleFill/>
                    Member
                </Space>
            ),
            children: (
                <MemberTab
                    notificationApi={notificationApi}
                    groupId={groupId}
                    leaderId={leaderId}
                />
            ),
        },
        {
            key: 'task',
            label: (
                <Space>
                    <MdAssignment/>
                    Task
                </Space>
            ),
            children: (
                <TaskTab
                    notificationApi={notificationApi}
                    groupId={groupId}
                    leaderId={leaderId}
                />
            ),
        },
        {
            key: 'file',
            label: (
                <Space>
                    <AiFillFile/>
                    File
                </Space>
            ),
            children: (
                <FileTab
                    notificationApi={notificationApi}
                    groupId={groupId}
                    leaderId={leaderId}
                />
            ),
        },
    ])

    return (
        <>
            <Tabs
                type="card"
                destroyInactiveTabPane={true}
                tabBarExtraContent={{
                    left: <Button type="text">{groupName}</Button>,
                    right: <Button type="text">{leaderId === accountId ? "Leader" : "Member"}</Button>,
                }}
                defaultActiveKey="post"
                items={tabs}
            />
        </>
    )
}