import {createSlice} from "@reduxjs/toolkit";

export const accountIdSlice = createSlice({
    name: "accountId",
    initialState: {
        value: 0
    },
    reducers: {
        updateAccountId: (state, action) => void(state.value = action.payload)
    }
})

export const {updateAccountId} = accountIdSlice.actions;

export const selectAccountId = (state) => state.accountId.value

export default accountIdSlice.reducer