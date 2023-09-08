import {forwardRef, useEffect, useImperativeHandle, useRef, useState} from "react";
import {Badge, List} from "antd";
import {formatTime} from "@/lib/Utils";

const ListItemRender = forwardRef(function ListItemRender(props, ref) {
    const {item, itemMeta, itemOnClick} = props
    const [unread, setUnread] = useState(item.readStatus === 'UNREAD')
    const [selected, setSelected] = useState(false)
    const originalStyle = {
        width: "95%"
    }
    const mouseEnterStyle = {
        backgroundColor: "#e6f4ff",
        ...originalStyle
    }
    const [selectedStyle] = useState(mouseEnterStyle)
    const [listItemStyle, setListItemStyle] = useState(originalStyle)

    useImperativeHandle(ref, () => {
        return {
            reportItemId() {
                console.log(item.id)
            },
            setSelectedStatus(selectedStatus) {
                setSelected(selectedStatus)
                if (!selectedStatus) {
                    setUnread(false)
                }
            }
        }
    }, [])

    return (
        <List.Item
            key={item.id}
            style={selected ? selectedStyle : listItemStyle}
            onMouseEnter={() => setListItemStyle(mouseEnterStyle)}
            onMouseLeave={() => setListItemStyle(originalStyle)}
            onClick={() => {
                itemOnClick(item.id, item.targetType)
                setUnread(false)
            }}
        >
            {itemMeta}
            <Badge dot={item.readStatus === 'UNREAD'}></Badge>
        </List.Item>
    )
})

export default function ListItemsRender({items, initialId, itemOnSelect}) {
    const itemRefs = useRef([])
    const itemRefMap = new Map()
    const [lastSelectedItem, setLastSelectedItem] = useState(null)
    const itemOnClick = (id, type) => {
        if (itemOnSelect) {
            itemOnSelect(id, type);
        }
        const itemRef = itemRefMap.get(id)
        // itemRef.reportItemId()
        // console.log(itemRef)
        if (lastSelectedItem !== null) {
            lastSelectedItem.setSelectedStatus(false)
        }
        itemRef.setSelectedStatus(true)
        setLastSelectedItem(itemRef)
    }

    useEffect(() => {
        const itemRef = itemRefMap.get(initialId)
        if (itemRef) {
            itemRef.setSelectedStatus(true);
            setLastSelectedItem(itemRef)
        }
    }, []);

    return items.map(item => {
        return (
            <ListItemRender
                ref={(ref) => {
                    // console.log(ref)
                    itemRefs.current.push(ref)
                    itemRefMap.set(item.id, ref)
                    return ref
                }}
                key={item.id}
                item={item}
                itemMeta={
                    <List.Item.Meta
                        title={item.title}
                        description={formatTime(item.createdTime)}
                    />
                }
                itemOnClick={itemOnClick}
            />
        )
    })
}