import {useEffect} from "react";
import EditorJS from "@editorjs/editorjs";
import Header from "@editorjs/header"
import Checklist from "@editorjs/checklist"
import NestedList from "@editorjs/nested-list"
import Quote from "@editorjs/quote"
import CodeTool from "@editorjs/code"
import InlineCode from "@editorjs/inline-code"
import Marker from "@editorjs/marker"
import Table from "@editorjs/table"
import Delimiter from "@editorjs/delimiter"
import Warning from "@editorjs/warning"
import SimpleImage from "@editorjs/simple-image"

export default function NoteEditor({content, updateContent}) {

    useEffect(() => {
        // console.log(typeof content)
        const editor = new EditorJS({
            holder: "noteEditor",
            placeholder: "Note is a good thing",
            data: content,
            tools: {
                header: {
                    class: Header,
                    config: {
                        placeholder: "Enter a header",
                        levels: [2, 3, 4],
                        defaultLevel: 3
                    }
                },
                checklist: {
                    class: Checklist,
                    inlineToolbar: true
                },
                list: {
                    class: NestedList,
                    inlineToolbar: true,
                    config: {
                        defaultStyle: 'unordered'
                    }
                },
                quote: {
                    class: Quote,
                    inlineToolbar: true,
                    config: {
                        quotePlaceholder: 'Enter a quote',
                        captionPlaceholder: 'Quote\'s author',
                    },
                },
                code: CodeTool,
                inlineCode: InlineCode,
                Marker: Marker,
                table: {
                    class: Table,
                    inlineToolbar: true,
                    config: {
                        rows: 2,
                        cols: 3,
                    },
                },
                delimiter: Delimiter,
                warning: {
                    class: Warning,
                    inlineToolbar: true,
                    config: {
                        titlePlaceholder: 'Title',
                        messagePlaceholder: 'Message',
                    },
                },
                image: SimpleImage
            },
            onChange: (api, event) => {
                api.saver.save()
                    .then(outputData => {
                        updateContent(outputData)
                    })
                    .catch(error => {
                        console.log(error)
                        updateContent(null)
                    })
            }
        })
    }, [])

    return (
        <>
            <div id="noteEditor"/>
        </>
    )
}