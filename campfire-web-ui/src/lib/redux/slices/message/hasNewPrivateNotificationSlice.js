import {createSlice} from "@reduxjs/toolkit";

export const hasNewPrivateNotificationSlice = createSlice({
    name: "hasNewPrivateNotification",
    initialState: {
        value: {
            hasNew: false,
            id: 0
        }
    },
    reducers: {
        updateHasNewPrivateNotification: (state, action) => void(state.value = action.payload),
        clearHasNewPrivateNotification: (state) => void(state.value = {hasNew: false, id: 0})
    }
})

export const {
    updateHasNewPrivateNotification,
    clearHasNewPrivateNotification} = hasNewPrivateNotificationSlice.actions;

export const selectHasNewPrivateNotification = (state) => state.hasNewPrivateNotification.value

export default hasNewPrivateNotificationSlice.reducer