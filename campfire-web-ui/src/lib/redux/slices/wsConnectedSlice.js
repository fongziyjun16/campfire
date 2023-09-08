import {createSlice} from "@reduxjs/toolkit";

export const wsConnectedSlice = createSlice({
    name: "wsConnected",
    initialState: {
        value: false
    },
    reducers: {
        updateWsConnected: (state, action) => void(state.value = action.payload)
    }
})

export const {updateWsConnected} = wsConnectedSlice.actions;

export const selectWsConnected = (state) => state.wsConnected.value

export default wsConnectedSlice.reducer