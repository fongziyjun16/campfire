import {createSlice} from "@reduxjs/toolkit";

export const hasMessageSlice = createSlice({
    name: "hasMessage",
    initialState: {
        value: false
    },
    reducers: {
        updateHasMessage: (state, action) => void(state.value = action.payload)
    }
})

export const {updateHasMessage} = hasMessageSlice.actions;

export const selectHasMessage = (state) => state.hasMessage.value

export default hasMessageSlice.reducer