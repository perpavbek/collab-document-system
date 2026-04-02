import { Editor } from '@tinymce/tinymce-react';
import 'tinymce/tinymce';
import 'tinymce/icons/default';
import 'tinymce/models/dom';
import 'tinymce/themes/silver';
import 'tinymce/plugins/advlist';
import 'tinymce/plugins/autolink';
import 'tinymce/plugins/autoresize';
import 'tinymce/plugins/link';
import 'tinymce/plugins/lists';
import 'tinymce/plugins/searchreplace';
import 'tinymce/plugins/wordcount';
import 'tinymce/skins/ui/oxide/skin.css';

interface CollaborativeEditorProps {
  value: string;
  readOnly?: boolean;
  onChange: (nextValue: string) => void;
}

export const CollaborativeEditor = ({ value, readOnly = false, onChange }: CollaborativeEditorProps) => {
  const isCompactViewport = typeof window !== 'undefined' && window.matchMedia('(max-width: 768px)').matches;
  const editorMinHeight = isCompactViewport ? 420 : 620;

  return (
    <div className="collab-tinymce flex min-h-[420px] min-w-0 w-full flex-1 flex-col rounded-[28px] border border-slate-200 bg-white shadow-soft">
      <div className="flex h-full min-h-0 min-w-0 w-full flex-1 overflow-hidden rounded-[28px] bg-white">
        <Editor
          disabled={readOnly}
          inline
          licenseKey="gpl"
          tagName="div"
          tinymceScriptSrc={undefined}
          init={{
            autoresize_bottom_margin: isCompactViewport ? 16 : 24,
            branding: false,
            content_css: false,
            content_style:
              ".mce-content-body { margin: 0; background: #ffffff; font-family: Georgia, 'Times New Roman', serif; font-size: 17px; line-height: 1.75; color: #10231b; } .mce-content-body p { margin: 0 0 1em; } .mce-content-body ul, .mce-content-body ol { margin: 0 0 1em; padding-left: 1.5em; } .mce-content-body ul { list-style: disc; } .mce-content-body ol { list-style: decimal; } .mce-content-body ul ul { list-style: circle; } .mce-content-body ul ul ul { list-style: square; } .mce-content-body ol ol { list-style: lower-alpha; } .mce-content-body ol ol ol { list-style: lower-roman; } .mce-content-body li { margin: 0.25em 0; }",
            elementpath: false,
            menubar: false,
            min_height: editorMinHeight,
            placeholder: 'Start writing your document with rich text formatting.',
            plugins: [
              'advlist',
              'autolink',
              'autoresize',
              'link',
              'lists',
              'searchreplace',
              'wordcount',
            ],
            quickbars_selection_toolbar: false,
            resize: false,
            toolbar_location: 'top',
            toolbar: readOnly
              ? false
              : 'undo redo | bold italic underline strikethrough | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | blockquote link | removeformat',
          }}
          onEditorChange={onChange}
          value={value}
        />
      </div>
    </div>
  );
};
