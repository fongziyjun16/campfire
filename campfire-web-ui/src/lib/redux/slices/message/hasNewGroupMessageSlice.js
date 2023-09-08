import {createSlice} from "@reduxjs/toolkit";

export const hasNewGroupMessageSlice = createSlice({
    name: "hasNewGroupMessage",
    initialState: {
        value: {
            hasNew: true,
            groupChatId: "",
            groupMessageId: ""
        }
    },
    reducers: {
        updateHasNewGroupMessage: (state, action) => void(state.value = action.payload),
        clearHasNewGroupMessage: (state) => void(state.value = {hasNew: false, groupChatId: "", groupMessageId: ""})
    }
})

export const {
    updateHasNewGroupMessage,
    clearHasNewGroupMessage} = hasNewGroupMessageSlice.actions;

export const selectHasNewGroupMessage = (state) => state.hasNewGroupMessage.value

export default hasNewGroupMessageSlice.reducer