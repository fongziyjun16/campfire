import {createSlice} from "@reduxjs/toolkit";

export const hasNewPublicNotificationSlice = createSlice({
    name: "hasNewPublicNotification",
    initialState: {
        value: {
            hasNew: false,
            id: 0
        }
    },
    reducers: {
        updateHasNewPublicNotification: (state, action) => void(state.value = action.payload),
        clearHasNewPublicNotification: (state) => void(state.value = {hasNew: false, id: 0})
    }
})

export const {
    updateHasNewPublicNotification,
    clearHasNewPublicNotification} = hasNewPublicNotificationSlice.actions;

export const selectHasNewPublicNotification = (state) => state.hasNewPublicNotification.value

export default hasNewPublicNotificationSlice.reducer