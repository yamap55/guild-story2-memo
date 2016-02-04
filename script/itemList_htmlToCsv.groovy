@Grab('org.jsoup:jsoup:1.8.3')
@Grab('com.xlson.groovycsv:groovycsv:1.1')
import org.jsoup.Jsoup
import com.xlson.groovycsv.CsvParser

def oldItemMasterPath = $/C:\work\workspace\guild-story2-search\guild-story2-search\master\item.csv/$

//def str = $/http://wikiwiki.jp/$
//println  new  URL(str).getText("EUC-JP") // 403
def wikiHtml = $/C:\work\20160131\item.html/$

def outputFilePath = $/C:\work\20160131\item.csv/$
def outputFile = new File(outputFilePath)

if (outputFile.exists()) {
  outputFile.delete()
}

// ���A�C�e���}�X�^�[����A�C�e�������擾����B
def getOldItemInfo = {
  def csv = new File(oldItemMasterPath).text
  def data = new CsvParser().parse(csv).collect{["����": it["����"],"���":it["���"], "��ʏڍ�":it["��ʏڍ�"], "�ǂ݂���":it["�ǂ݂���"]]}
  return {name->
    name ? data.find {it["����"] == name} : null
  }
}()

// List��collectWithIndex��ǉ��B
List.metaClass.collectWithIndex = {body->
    def i=0
    delegate.collect { body(it, i++) }
}


// ���ʂ���e�X�e�[�^�X�𔲂��o�����߂̐��K�\��
def statusRegex = [/�U����(-?[.0-9]+)$/,/�������x(-?[.0-9]+)$/,/�h���(-?[.0-9]+)$/,/���\��(-?[.0-9]+)$/,/���@�U����(-?[.0-9]+)$/,/���@�h���(-?[.0-9]+)$/,/�K�E��(-?[.0-9]+%)$/,/�U����(-?[.0-9]+)$/,/�ő�HP(-?[.0-9]+)$/,/㩉����\��(-?[.0-9]+)$/,/���@�񕜗�(-?[.0-9]+)$/]

// �ŏI�I��HEAD
// ID,���,��ʏڍ�,No.,����,�ǂ݂���,�h���b�v,����,���i,
// �U����,�������x,�h���,���\��,���@�U����,���@�h���,�K�E��,�U����,�ő�HP,㩉����\��,���@�񕜗�
def masterHead = ["ID", "���", "��ʏڍ�", "NO.", "����", "�ǂ݂���", "�h���b�v", "����", "���i", "�U����", "�������x", "�h���", "���\��", "���@�U����", "���@�h���", "�K�E��", "�U����", "�ő�HP", "㩉����\��", "���@�񕜗�"]
def statusIndex = masterHead.indexOf("�U����")
def createRecode = {head, data ->
  def result = masterHead.collect{""}
  head.eachWithIndex {d, i ->
    if (d == "����") {
      def a = []
      data[i].split("�@").each {str ->
        def flag = false
        statusRegex.eachWithIndex { regex, j ->
          def m = str =~ regex
          if (m) {
            result[j + statusIndex] = m[0][1] - "%"
            flag = true
          }
        }
        if (!flag) {
          a << str
        }
      }
      result[masterHead.indexOf(d)] = a ? a.join("�@") : ""
    } else {
      // �e�[�u������������Ă���null��Ԃ����Ƃ����邽�߃f�[�^���Ȃ��ꍇ�ɂ͋󕶎��ɕϊ��B
      result[masterHead.indexOf(d)] = data[i] ?:""
    }
  }
  // �w����͌Â�ItemInfo����擾
  def name = result[masterHead.indexOf("����")]
  def oldInfo = getOldItemInfo(name)
  if (oldInfo) {
    def f = {n ->
      result[masterHead.indexOf(n)] = oldInfo[n]
    }
    ["���", "��ʏڍ�", "�ǂ݂���"].each{f(it)}
  }
  result
}

def doc = Jsoup.parse(new File(wikiHtml), "EUC-JP")
def l = []
doc.select(".style_table").each {
  def head = it.select("thead th").collect{it.text()}
  def body = it.select("tbody tr").each {
    def data = it.select("td").collectWithIndex {v, i ->
      def d = v.text()
      if(head[i] == "���i" || head[i] == "���l") {
        head[i] = "���i" // ���i�ɓ���
        d = d.contains(",")?"\"${d}\"":d
      } else if (head[i] == "����") {
        // ���ʂ̒��ɉ��s���܂�ł��邱�Ƃ����邽�߁B
        d = v.html().replaceAll('<br class="spacer">', "�@")
      } else if (head[i] == "NO.") { 
        d = d as int
      } else if (head[i] == "���l"){
        head[i] = "�h���b�v"
      }
      d
    }
    l << createRecode(head, data)
  }
}
outputFile << masterHead.join(",")
outputFile << "\r\n"
l.eachWithIndex {d, i ->
  d[0] = i+1  //ID
  outputFile << d.join(",")
  outputFile << "\r\n"
}