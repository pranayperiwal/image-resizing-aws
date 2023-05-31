import formidable from "formidable";
import fs from "fs/promises";
import fs2 from "fs";
import path from "path";
import FormData from "form-data";

export const config = {
  api: {
    bodyParser: false,
  },
};

const readFile = async (req, saveLocally) => {
  const options = {};
  if (saveLocally) {
    options.filename = (name, ext, path, form) => {
      return Date.now().toString() + "_" + path.originalFilename;
    };
  }
  options.maxFileSize = 4000 * 1024 * 1024;
  const form = formidable(options);
  return new Promise((resolve, reject) => {
    form.parse(req, (err, fields, files) => {
      if (err) reject(err);
      resolve({ files });
    });
  });
};

const sendToServer = async (form, res, filename) => {
  fetch("http://54.95.177.46:8080/processImage/process", {
    method: "POST",
    body: form,
  })
    .then((response) => {
      return response.buffer();
    })
    .then((buf) => {
      res.setHeader("Content-Type", "image/jpeg");
      res.status(200).send(buf);
    })
    .catch((err) => {
      console.log(err);
      res.status(405).json({ error: "Something went wrong" });
    });
};

const handler = async (req, res) => {
  try {
    await fs.readdir(path.join(process.cwd() + "/public", "/images"));
  } catch (error) {
    await fs.mkdir(path.join(process.cwd() + "/public", "/images"));
  }
  await readFile(req, true).then(({ files }) => {
    var form = new FormData();
    form.append("file", fs2.createReadStream(files.file.filepath));
    sendToServer(form, res, files.file.newFilename);
  });
};

export default handler;
