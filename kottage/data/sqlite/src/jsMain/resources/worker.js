import initSqlJs from "@jlongster/sql.js";
import {SQLiteFS} from "absurd-sql";
import IndexedDBBackend from "absurd-sql/dist/indexeddb-backend";

let db = null;

async function run() {
    let SQL = await initSqlJs({locateFile: file => file});
    let sqlFS = new SQLiteFS(SQL.FS, new IndexedDBBackend());
    SQL.register_for_idb(sqlFS);

    SQL.FS.mkdir('/sql');
    SQL.FS.mount(sqlFS, {}, '/sql');

    const path = '/sql/db.sqlite';
    if (typeof SharedArrayBuffer === 'undefined') {
        let stream = SQL.FS.open(path, 'a+');
        await stream.node.contents.readIfFallback();
        SQL.FS.close(stream);
    }

    db = new SQL.Database(path, {filename: true});
    db.exec(`PRAGMA journal_mode=MEMORY;`);
    db.exec(`PRAGMA page_size=8192;`);
}

function onModuleReady() {
    let data;
    data = this["data"];
    const config = data["config"] ? data["config"] : {};
    switch (data && data["action"]) {
        case "exec":
            if (!data["sql"]) {
                throw "exec: Missing query string";
            }
            return postMessage({
                id: data["id"],
                results: db.exec(data["sql"], data["params"], config)
            });
        default:
            throw new Error("Invalid action : " + (data && data["action"]));
    }
}

function onError(err) {
    return postMessage({
        id: this["data"]["id"],
        error: err["message"]
    });
}

if (typeof importScripts === "function") {
    db = null;
    const sqlModuleReady = run()
    self.onmessage = (event) => {
        return sqlModuleReady
            .then(onModuleReady.bind(event))
            .catch(onError.bind(event));
    }
}
