<template>
  <footer>
    <div class="container">
      <div class="box">
        <dl class="box-dl">
          <dt>员工培训与学习平台</dt>
          <dd><a href="#" target="_blank">关于我们</a></dd>
          <dd><a href="#" target="_blank">联系我们</a></dd>
          <dd><a href="#" target="_blank">帮助中心</a></dd>
        </dl>
        <dl class="box-dl">
          <dt>学习资源</dt>
          <dd><a href="#" target="_blank">图书</a></dd>
          <dd><a href="#" target="_blank">课程</a></dd>
          <dd><a href="#" target="_blank">直播</a></dd>
        </dl>
        <dl class="box-dl">
          <dt>服务支持</dt>
          <dd><a href="#" target="_blank">企业培训</a></dd>
          <dd><a href="#" target="_blank">人才评测</a></dd>
          <dd><a href="#" target="_blank">技能认证</a></dd>
        </dl>
        <dl class="box-dl">
          <dt>联系我们</dt>
          <dd><a href="#" target="_blank">电话：400-618-9090</a></dd>
          <dd><a href="#" target="_blank">邮箱：training@company.com</a></dd>
          <dd><a href="#" target="_blank">地址：北京市海淀区</a></dd>
        </dl>
      </div>
      <div class="footer-link">
        <span>© 2025 员工培训与学习平台 版权所有</span>
      </div>
    </div>
  </footer>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "PortalFooter"
})
export default class extends Vue {}
</script>

<style lang="scss" scoped>
footer {
  width: 100%;
  background: #fff;
  margin-top: 20px;
  padding: 40px 0;
}

.box {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.box-dl {
  float: none;
  width: 25%;
  padding: 0 15px;
}

.box-dl dt {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
  line-height: 1.5;
}

.box-dl dd {
  margin-bottom: 10px;
  line-height: 1.8;
}

.footer-link {
  clear: both;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
  line-height: 1.5;
}
</style>