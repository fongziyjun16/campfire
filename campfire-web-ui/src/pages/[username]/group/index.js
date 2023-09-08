import MainLayout from "@/pages/[username]/layout";
import {Layout, notification, Typography} from "antd";
import GroupList from "@/pages/[username]/group/componenet/GroupList";
import GroupContent from "@/pages/[username]/group/componenet/GroupContent";
import {useState} from "react";
import JoinGroup from "@/pages/[username]/group/componenet/JoinGroup";

const {Sider, Content} = Layout
const {Title} = Typography

function GroupPage() {

    const [notificationApi, notificationContextHolder] = notification.useNotification()
    const [selectedGroupId, setSelectedGroupId] = useState("")
    const [selectedGroupName, setSelectedGroupName] = useState("")
    const [selectedGroupLeaderId, setSelectedGroupLeaderId] = useState("")
    const [mode, setMode] = useState("")

    const clickJoin = () => {
        setMode("join")
    }

    const selectGroup = (groupId, groupName, leaderId) => {
        setMode("group")
        setSelectedGroupId(groupId)
        setSelectedGroupName(groupName)
        setSelectedGroupLeaderId(leaderId)
    }

    return (
        <>
            {notificationContextHolder}
            <Layout style={{height: "100%"}}>
                <Sider
                    width={300}
                    style={{backgroundColor: "white"}}
                >
                    <GroupList
                        notificationApi={notificationApi}
                        clickJoin={clickJoin}
                        selectGroup={selectGroup}
                    />
                </Sider>
                <Content style={{backgroundColor: "white", overflow: "auto"}}>
                    {
                        mode === "" ?
                            <>
                                <Typography>
                                    <Title style={{paddingLeft: "10px"}}>
                                        Time to select a group or Join a new Group
                                    </Title>
                                </Typography>
                            </> :
                            (
                                mode === "join" ?
                                    <JoinGroup
                                        notificationApi={notificationApi}
                                    /> :
                                    <GroupContent
                                        key={selectedGroupId}
                                        notificationApi={notificationApi}
                                        groupId={selectedGroupId}
                                        groupName={selectedGroupName}
                                        leaderId={selectedGroupLeaderId}
                                    />
                            )
                    }
                </Content>
            </Layout>
        </>
    )
}

GroupPage.pageLayout = MainLayout

export default GroupPage