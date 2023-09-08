import MainLayout from "@/pages/[username]/layout";
import {Col, notification, Row, Tabs} from "antd";
import ContactTab from "@/pages/[username]/contact/tabs/ContactTab";
import WaitingListTab from "@/pages/[username]/contact/tabs/WaitingListTab";
import ContactRequestTab from "@/pages/[username]/contact/tabs/ContactRequestTab";
import AddContactTab from "@/pages/[username]/contact/tabs/AddContactTab";

function ContactPage() {

    const [notificationApi, notificationContextHolder] = notification.useNotification();

    return (
        <>
            {notificationContextHolder}
            <Row>
                <Col span={4}></Col>
                <Col span={16}>
                    <Tabs
                        tabPosition="left"
                        items={[
                            {
                                key: "contact",
                                label: "Contact",
                                children: <ContactTab notification={notificationApi}/>
                            },
                            {
                                key: "addContact",
                                label: "Add Contact",
                                children: <AddContactTab notification={notificationApi}/>
                            },
                            {
                                key: "waitingList",
                                label: "Waiting List",
                                children: <WaitingListTab notification={notificationApi}/>
                            },
                            {
                                key: "contactRequest",
                                label: "Contact Request",
                                children: <ContactRequestTab notification={notificationApi}/>
                            }
                        ]}
                        style={{paddingTop: "10px"}}
                    />
                </Col>
                <Col span={4}></Col>
            </Row>
        </>
    )
}

ContactPage.pageLayout = MainLayout

export default ContactPage

