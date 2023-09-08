import {createSlice} from "@reduxjs/toolkit";

export const accountStatusSlice = createSlice({
    name: "accountStatus",
    initialState: {
        value: 0
    },
    reducers: {
        updateAccountStatus: (state, action) => void(state.value = action.payload)
    }
})

export const {updateAccountStatus} = accountStatusSlice.actions;

export const selectAccountStatus = (state) => state.accountStatus.value

export default accountStatusSlice.reducer