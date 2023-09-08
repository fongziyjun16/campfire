import {createSlice} from "@reduxjs/toolkit";

export const roleNamesSlice = createSlice({
    name: "roleNames",
    initialState: {
        value: []
    },
    reducers: {
        updateRoleNames: (state, action) => void(state.value = action.payload)
    }
})

export const {updateRoleNames} = roleNamesSlice.actions

export const selectRoleNames = (state) => state.roleNames.value

export default roleNamesSlice.reducer