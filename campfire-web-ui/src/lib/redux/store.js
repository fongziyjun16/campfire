import storage from "redux-persist/lib/storage"
import {combineReducers, configureStore, getDefaultMiddleware} from "@reduxjs/toolkit";
import accountIdReducer from "@/lib/redux/slices/accountIdSlice";
import usernameReducer from "@/lib/redux/slices/usernameSlice";
import accountStatusReducer from "@/lib/redux/slices/accountStatusSlice";
import avatarReducer from "@/lib/redux/slices/avatarUrlSlice";
import roleNamesReducer from "@/lib/redux/slices/roleNamesSlice";
import hasMessageReducer from "@/lib/redux/slices/message/hasMessageSlice";
import wsConnectedReducer from "@/lib/redux/slices/wsConnectedSlice";
import hasNewPublicNotificationReducer from "@/lib/redux/slices/message/hasNewPublicNotificationSlice";
import hasNewPrivateNotificationReducer from "@/lib/redux/slices/message/hasNewPrivateNotificationSlice";
import hasNewContactMessageReducer from "@/lib/redux/slices/message/hasNewContactMessageSlice";
import hasNewGroupMessageReducer from "@/lib/redux/slices/message/hasNewGroupMessageSlice";
import {persistReducer, persistStore} from "redux-persist";

const persistConfig = {
    key: 'app',
    storage
}

const reducers = combineReducers({
    accountId: accountIdReducer,
    username: usernameReducer,
    accountStatus: accountStatusReducer,
    roleNames: roleNamesReducer,
    avatarUrl: avatarReducer,
    hasMessage: hasMessageReducer,
    wsConnected: wsConnectedReducer,
    hasNewPublicNotification: hasNewPublicNotificationReducer,
    hasNewPrivateNotification: hasNewPrivateNotificationReducer,
    hasNewContactMessage: hasNewContactMessageReducer,
    hasNewGroupMessage: hasNewGroupMessageReducer
})

const persistedReducer = persistReducer(persistConfig, reducers)

export default function makeStore() {
    let store = configureStore({
        reducer: persistedReducer,
        middleware: getDefaultMiddleware({
            serializableCheck: false
        })
    })
    let persistor = persistStore(store)
    return {store, persistor}
}