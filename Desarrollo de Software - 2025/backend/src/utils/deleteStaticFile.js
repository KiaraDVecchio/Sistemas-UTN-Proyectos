import { unlink } from "fs";
import path from "path";

export function deleteStaticFile(fileName) {
    const UPLOADS_DIR = path.join(process.cwd(), 'uploads');

    unlink(path.join(UPLOADS_DIR, fileName), (err) => {
        if (err) {
            console.error(err);
        }
    })
}