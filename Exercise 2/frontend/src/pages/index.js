import { useState } from "react";
import { RotatingLines } from "react-loader-spinner";

export default function Home() {
  const [file, setFile] = useState();
  const [loading, setLoading] = useState(false);

  const handleFileChange = (e) => {
    if (e.target.files) {
      setFile(e.target.files[0]);
    }
  };

  const handleUploadClick = () => {
    if (!file) {
      return;
    }
    if (file.type.split("/")[0] != "image") {
      return;
    }

    setLoading(true);

    const formData = new FormData();
    formData.append("file", file);

    fetch("api/processFile", {
      method: "POST",
      body: formData,
    })
      .then((res) => {
        console.log(res);
        setLoading(false);
        return res.blob();
      })
      .then((blob) => {
        console.log(blob);
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "image_processed.jpeg";
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  return (
    <main className={`flex min-h-screen flex-col items-center  p-24`}>
      <h1 style={{ fontSize: 40 }} className="font-mono">
        COMP3358 Assignment 4
      </h1>
      <div
        style={{
          // border: "1px solid black",
          height: 200,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <input
          style={{ border: "1px solid black" }}
          type="file"
          onChange={handleFileChange}
        />

        <button
          onClick={handleUploadClick}
          style={{
            backgroundColor: "black",
            color: "white",
            height: 40,
            paddingLeft: 20,
            paddingRight: 20,
            borderRadius: 5,
            marginTop: 20,
            marginBottom: 30,
          }}
        >
          Upload
        </button>
      </div>
      {loading && (
        <div>
          <RotatingLines
            style={{ marginTop: 20 }}
            strokeColor="grey"
            strokeWidth="5"
            animationDuration="0.75"
            width="50"
            visible={true}
          />
        </div>
      )}
    </main>
  );
}
