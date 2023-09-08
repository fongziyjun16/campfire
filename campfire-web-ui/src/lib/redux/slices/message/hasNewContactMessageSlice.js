import {createSlice} from "@reduxjs/toolkit";

export const hasNewContactMessageSlice = createSlice({
    name: "hasNewContactMessage",
    initialState: {
        value: {
            hasNew: true,
            id: "",
            contactMessageId: ""
        }
    },
    reducers: {
        updateHasNewContactMessage: (state, action) => void(state.value = action.payload),
        clearHasNewContactMessage: (state) => void(state.value = {hasNew: false, id: "", contactMessageId: ""})
    }
})

export const {
    updateHasNewContactMessage,
    clearHasNewContactMessage} = hasNewContactMessageSlice.actions;

export const selectHasNewContactMessage = (state) => state.hasNewContactMessage.value

export default hasNewContactMessageSlice.reducer