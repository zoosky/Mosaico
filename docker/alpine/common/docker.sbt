val abuild = (project in file("..")/"abuild").enablePlugins(MosaicoDockerPlugin)

imageNames in docker := Seq(ImageName(s"sciabarra/alpine-common:1"))

dockerfile in docker := {
  val alpBuild = (docker in abuild).value
  new Dockerfile {
    from("alpine:edge")
    copy(alpBuild.toTask(s" ${alpBuild} daemontools daemontools.apk").value, "/tmp/")
    runRaw(s"""apk update && apk add git curl sudo""")
    copy(alpBuild.toTask(s" ${alpBuild} daemontools daemontools.apk").value, "/tmp/")
    runRaw("apk add --allow-untrusted /tmp/*.apk  && rm /tmp/*.apk")
    cmd("/usr/bin/svscan", "/services/")
  }
}
