import {useEffect, useState} from "react";
import {Button, Card, Divider, List} from "antd";
import {AiOutlineUsergroupAdd} from "react-icons/ai";
import InfiniteScroll from 'react-infinite-scroll-component';
import axios from "axios";

export default function GroupList({notificationApi, clickJoin, selectGroup}) {

    const [groupTotalNumber, setGroupTotalNumber] = useState(0)
    const [groupListLoading, setGroupListLoading] = useState(false)
    const [groups, setGroups] = useState([])

    useEffect(() => {
        loadGroups()
    }, []);

    const loadGroups = () => {
        setGroupListLoading(true)
        axios.get(
            "/api/group/joining-in",
            {
                params: {
                    size: 30,
                    havingSize: groups.length
                }
            }
        ).then(resp => {
            if (resp.data?.data) {
                const {total, groupHeads: newGroupHeads} = resp.data.data
                setGroupTotalNumber(total)
                if (groups.length === 0) {
                    setGroups([...newGroupHeads])
                } else {
                    const arr = [...newGroupHeads]
                    arr.forEach(element => groups.push(element))
                    setGroups([...groups])
                }
            }
        }).catch(err => {
            console.log(err)
            notificationApi.error({
                message: "Load Groups Error. Try again later"
            })
        }).finally(() => {
            setGroupListLoading(false)
        })
    }

    const loadMoreGroups = () => {
        // console.log("load more")
        loadGroups()
    }

    return (
        <>
            <Card
                title="Your Groups"
                extra={<>
                    <Button
                        icon={<AiOutlineUsergroupAdd/>}
                        onClick={() => clickJoin()}
                    >
                        Join
                    </Button>
                </>}
                style={{height: "100%"}}
            >
                <div
                    id="scrolableDiv"
                    style={{height: "75vh", overflow: "auto"}}
                >
                    <InfiniteScroll
                        next={loadMoreGroups}
                        hasMore={groups.length < groupTotalNumber}
                        loader={null}
                        dataLength={groups.length}
                        endMessage={<Divider plain>No More Groups</Divider>}
                        scrollableTarget="scrolableDiv"
                    >
                        <List
                            loading={groupListLoading}
                            locale={{emptyText: <span></span>}}
                            dataSource={groups}
                            renderItem={group => (
                                <List.Item key={group.id}>
                                    <Button
                                        type="text"
                                        onClick={() => {
                                            selectGroup(group.id, group.name, group.leaderId)
                                        }}
                                        style={{
                                            width: "100%",
                                            textAlign: "left"
                                        }}
                                    >
                                        {group.name}
                                    </Button>
                                </List.Item>
                            )}
                        />
                    </InfiniteScroll>
                </div>
            </Card>
        </>
    )
}